package nl.the_experts.keycloak.configuration.devnt;

import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import nl.the_experts.keycloak.configuration.KeycloakConfigurationProperties;
import nl.the_experts.keycloak.configuration.devnt.clients.ClientConfiguration;
import nl.the_experts.keycloak.configuration.devnt.clients.ClientConfigurationOptions;
import nl.the_experts.keycloak.configuration.devnt.clients.ClientConfigurationOptionsValidator;
import nl.the_experts.keycloak.configuration.devnt.userFederations.DevntActiveDirectoryConfiguration;
import nl.the_experts.keycloak.configuration.devnt.userFederations.DevntActiveDirectoryConfigurationOptions;
import org.keycloak.admin.client.Keycloak;

import java.util.Optional;

@JBossLog
@AllArgsConstructor
public class DevntConfiguration {
    private final KeycloakConfigurationProperties configuration;
    private final Keycloak keycloak;

    public void configure() {
        var realmName = configuration.get("DEVNT_REALM_NAME");
        var realmDisplayName = configuration.get("DEVNT_REALM_DISPLAY_NAME");

        var iotClientOptions = ClientConfigurationOptions.builder()
                .id(configuration.get("DEVNT_CLIENT_IOT_ID"))
                .name(configuration.get("DEVNT_CLIENT_IOT_NAME"))
                .authType(configuration.get("DEVNT_CLIENT_IOT_AUTH_TYPE"))
                .clientSECRET(configuration.get("DEVNT_CLIENT_IOT_SECRET"))
                .redirectUris(Optional.ofNullable(configuration.get("DEVNT_CLIENT_IOT_REDIRECT_URIS")).orElse("*"))
                .build();

        var nextCloudClientOptions = ClientConfigurationOptions.builder()
                .id(configuration.get("DEVNT_CLIENT_NEXTCLOUD_ID"))
                .name(configuration.get("DEVNT_CLIENT_NEXTCLOUD_NAME"))
                .authType(configuration.get("DEVNT_CLIENT_NEXTCLOUD_AUTH_TYPE"))
                .clientSECRET(configuration.get("DEVNT_CLIENT_NEXTCLOUD_SECRET"))
                .redirectUris(Optional.ofNullable(configuration.get("DEVNT_CLIENT_NEXTCLOUD_REDIRECT_URIS")).orElse("*"))
                .build();

        var clientConfigurationOptionsValidator = new ClientConfigurationOptionsValidator();
        try {
            clientConfigurationOptionsValidator.validate(iotClientOptions);
            clientConfigurationOptionsValidator.validate(nextCloudClientOptions);
        } catch (Exception e) {
            log.error("Error validating client configuration options", e);

            throw new RuntimeException(e);
        }

        var adOptions = DevntActiveDirectoryConfigurationOptions.builder()
                .realmName(realmName)
                .id(configuration.get("DEVNT_AD_ID"))
                .name(configuration.get("DEVNT_AD_NAME"))
                .host(configuration.get("DEVNT_AD_HOST"))
                .bindDN(configuration.get("DEVNT_AD_BIND_DN"))
                .bindCredentials(configuration.get("DEVNT_AD_BIND_CREDENTIALS"))
                .usersDN(configuration.get("DEVNT_AD_USERS_DN"))
                .build();

        log.info("-----------------------------------------------");
        log.infof("Starting configuration of realm '%s'.", realmName);
        log.info("-----------------------------------------------");

        new RealmConfiguration(keycloak.realms()).configure(realmName, realmDisplayName);
        new ClientConfiguration(iotClientOptions, keycloak.realm(realmName).clients()).configure();
        new ClientConfiguration(nextCloudClientOptions, keycloak.realm(realmName).clients()).configure();
        new DevntActiveDirectoryConfiguration(adOptions, keycloak.realm(realmName).components()).configure();

        log.info("-----------------------------------------------");
        log.infof("Finished configuration of realm '%s'.%n", realmName);
        log.info("-----------------------------------------------");
    }
}