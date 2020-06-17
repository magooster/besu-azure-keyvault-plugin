package net.iaminnovative.besu;

import com.google.auto.service.AutoService;
import com.google.common.base.Suppliers;
import net.iaminnovative.besu.configuration.AzureKVCLIOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.plugin.BesuContext;
import org.hyperledger.besu.plugin.BesuPlugin;

import org.hyperledger.besu.plugin.services.PicoCLIOptions;
import org.hyperledger.besu.plugin.services.SecurityModuleService;
import org.hyperledger.besu.plugin.services.securitymodule.SecurityModule;

import java.util.Optional;

@AutoService(BesuPlugin.class)
public class AzureKeyVaultPlugin implements BesuPlugin {

    private static final Logger LOG = LogManager.getLogger();
    private static final String PLUGIN_NAME = "azure-key-vault";

    private final AzureKVCLIOptions options;
    private BesuContext context;
    //private SecurityModule azureSM;

    public AzureKeyVaultPlugin() {
        this.options = AzureKVCLIOptions.create();
    }

    @Override
    public Optional<String> getName() {
        return Optional.of("AKV Secret Security Module");
    }

    @Override
    public void register(final BesuContext context) {
        LOG.info("Registering Azure Key Vault Security Module Plugin");

        this.context = context;

        context
                .getService(PicoCLIOptions.class)
                .ifPresentOrElse(
                        this::createPicoCLIOptions, () -> LOG.error("Could not obtain PicoCLIOptionsService"));


        context
                .getService(SecurityModuleService.class)
                .ifPresentOrElse(
                        this::createAndRegister,
                        () -> LOG.error("Failed to register Security Module due to missing SecurityModuleService."));
    }

    @Override
    public void start() {
        LOG.debug("Staring keyVault Plugin with configuration: {}", options.toString());
    }

    @Override
    public void stop() {
        LOG.debug("Stopping plugin.");
    }

    private void createAndRegister(final SecurityModuleService service) {

        service.register(PLUGIN_NAME, Suppliers.memoize(this::getSecurityModule)::get);

    }

    private SecurityModule getSecurityModule() {
        return AzureKVSecurityModule.getSecurityModule(options);
    }

    private void createPicoCLIOptions(final PicoCLIOptions picoCLIOptions) {
        picoCLIOptions.addPicoCLIOptions(PLUGIN_NAME, options);
    }


}
