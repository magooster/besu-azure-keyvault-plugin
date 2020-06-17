package net.iaminnovative.besu;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.identity.*;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import net.iaminnovative.besu.configuration.AzureKVCLIOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.crypto.KeyPairSecurityModule;
import org.hyperledger.besu.crypto.SECP256K1;
import org.hyperledger.besu.plugin.services.securitymodule.SecurityModuleException;
import org.hyperledger.besu.plugin.services.securitymodule.SecurityModule;

public class AzureKVSecurityModule {

    public static final String UNAUTHORIZED_ERROR = "Key Vault Secret operation is forbidden by policy";

    private static final Logger LOG = LogManager.getLogger();
    private static KeyPairSecurityModule securityModule;

    public synchronized static SecurityModule getSecurityModule(AzureKVCLIOptions options) {
        if (securityModule == null) {
            LOG.debug("Initializing Azure Key Vault");
            securityModule = initialize(options);
        }
        return securityModule;
    }

    private static KeyPairSecurityModule initialize(AzureKVCLIOptions options) throws SecurityModuleException {

        ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
                .clientId(options.clientId)
                .build();

        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(options.clientId)
                .clientSecret(options.clientSecret)
                .tenantId(options.tenantId)
                .build();

        ChainedTokenCredential credentialChain = new ChainedTokenCredentialBuilder()
                .addLast(managedIdentityCredential)
                .addLast(clientSecretCredential)
                .build();

        HttpClient httpClient = new OkHttpAsyncHttpClientBuilder().build();

        SecretClient client = new SecretClientBuilder()
                .vaultUrl(options.vaultUrl)
                .httpClient(httpClient)
                .credential(credentialChain)
                .buildClient();

        SECP256K1.KeyPair keyPair;
        try {
            KeyVaultSecret secret = client.getSecret(options.secretName);
            keyPair = SECP256K1.KeyPair.create(SECP256K1.PrivateKey.create(Bytes32.fromHexString(secret.getValue())));
            LOG.info(
                    "Loaded public key {} from vault {}",
                    keyPair.getPublicKey().toString(),
                    client.getVaultUrl());
            return new KeyPairSecurityModule(keyPair);
        }
        catch (HttpResponseException ex) {
            if (ex.getResponse().getStatusCode() == 404) {
                try {
                    keyPair = SECP256K1.KeyPair.generate();
                    client.setSecret(options.secretName, keyPair.getPrivateKey().getEncodedBytes().toString());
                    LOG.info(
                            "Generated new public key {} and stored it to vault {}",
                            keyPair.getPublicKey().toString(),
                            client.getVaultUrl());
                    return new KeyPairSecurityModule(keyPair);
                } catch (HttpResponseException e) {
                    if (e.getResponse().getStatusCode() == 403) {
                        throw new SecurityModuleException(UNAUTHORIZED_ERROR);
                    }
                    throw new SecurityModuleException("Azure Key Vault Error", e);
                }
            }
            if (ex.getResponse().getStatusCode() == 403) {
                throw new SecurityModuleException(UNAUTHORIZED_ERROR);
            }
            throw new SecurityModuleException("Azure Key Vault Error", ex);
        }

    }
}
