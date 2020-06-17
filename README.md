# Azure Key Vault Security Module Plugin

## Purpose of the Plugin
Persist a Besu node key in an Azure Key Vault Secret.  

Cannot at this time use Azure Key Vault Key as Azure Key Vault does not support the 
ECDH operation required by a Besu security module.

### Services Used
- **PicoCLIOptions** 
  * To configure the plugin
- **SecurityModuleSystem** 
  * To 


### Plugin Lifecycle
- **Register** 
  * Register the plugin
- **Start** 
  * Not Used
- **Stop** 
  * Not Used


## To Build the Plugin

Build the plugin jar
```
./gradlew build
```

Install the plugin into `$BESU_HOME`

```
mkdir $BESU_HOME/plugins
cp build/libs/*.jar $BESU_HOME/plugins
```

Run the Besu node 
```
$BESU_HOME/bin/besu --config-file=options.toml
```

### Pluging Configuration Options

#### Security Module plugin to use
--security-module="azure-key-vault"

#### Azure Key Vault url
--plugin-azure-key-vault-url="https://<VAULT_NAME>.vault.azure.net/"

#### Name of the secret in Azure Key Vault
--lugin-azure-key-vault-secret-name="besu"

#### Azure Service principal's app id
--plugin-azure-key-vault-client-id=""

#### id of the principal's Azure Active Directory tenant
--plugin-azure-key-vault-tenant-id=""

#### one of the service principal's client secrets
--plugin-azure-key-vault-client-secret=""

### Running in Azure

If using a system manged identity only the vault-url and secret-name are required.
If using a user assigned identity also need to configure the client-id.

## Disclaimer

This is very much a demo for others to learn from - use at your own risk..



