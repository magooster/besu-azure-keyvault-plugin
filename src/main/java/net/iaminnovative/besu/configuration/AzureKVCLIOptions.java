package net.iaminnovative.besu.configuration;

import picocli.CommandLine.Option;

public class AzureKVCLIOptions {

    private static final String VAULT_URL = "--plugin-azure-key-vault-url";
    private static final String SECRET_NAME = "--plugin-azure-key-vault-secret-name";
    private static final String CLIENT_ID = "--plugin-azure-key-vault-client-id";
    private static final String TENANT_ID = "--plugin-azure-key-vault-tenant-id";
    private static final String CLIENT_SECRET = "--plugin-azure-key-vault-client-secret";

    @Option(
            names = {VAULT_URL},
            paramLabel = "<STRING>",
            description = "The key vault url")
    public String vaultUrl;

    @Option(
            names = {SECRET_NAME},
            paramLabel = "<STRING>",
            description = "The secret name")
    public String secretName;

    @Option(
            names = {CLIENT_ID},
            paramLabel = "<STRING>",
            description = "The azure service principals app id")
    public String clientId;

    @Option(
            names = {CLIENT_SECRET},
            paramLabel = "<STRING>",
            description = "The azure service principals app secret")
    public String clientSecret;

    @Option(
            names = {TENANT_ID},
            paramLabel = "<STRING>",
            description = "The azure tenant id")
    public String tenantId;

    private AzureKVCLIOptions() {}

    public static AzureKVCLIOptions create() {
        return new AzureKVCLIOptions();
    }
}
