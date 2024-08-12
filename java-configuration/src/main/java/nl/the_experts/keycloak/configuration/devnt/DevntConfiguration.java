package nl.the_experts.keycloak.configuration.devnt;

import lombok.AllArgsConstructor;
import nl.the_experts.keycloak.configuration.KeycloakConfigurationProperties;
import nl.the_experts.keycloak.configuration.devnt.clients.ClientConfiguration;
import nl.the_experts.keycloak.configuration.devnt.clients.ClientConfigurationOptions;
import nl.the_experts.keycloak.configuration.devnt.clients.ClientOptionsValidator;
import nl.the_experts.keycloak.configuration.devnt.realm.RealmConfiguration;
import nl.the_experts.keycloak.configuration.devnt.realm.RealmConfigurationOptions;
import nl.the_experts.keycloak.configuration.devnt.realm.RealmEmailOptions;
import nl.the_experts.keycloak.configuration.devnt.realm.RealmOptionsValidator;
import nl.the_experts.keycloak.configuration.devnt.userFederations.DevntActiveDirectoryConfiguration;
import nl.the_experts.keycloak.configuration.devnt.userFederations.DevntActiveDirectoryConfigurationOptions;
import nl.the_experts.keycloak.configuration.devnt.userFederations.DevntActiveDirectoryGroupMapperOptions;
import nl.the_experts.keycloak.validation.ValidationException;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class DevntConfiguration {
    private static final String startTemplate = """
            -----------------------------------------------
            Starting configuration of realm '%s'.
            -----------------------------------------------
            """;
    private static final String endTemplate = """
            -----------------------------------------------
            Finished configuration of realm '%s'.
            -----------------------------------------------
            """;
    private static final Logger logger = Logger.getLogger(DevntConfiguration.class);

    private final KeycloakConfigurationProperties configuration;
    private final Keycloak keycloak;

    public void configure() {
        var realmOptions = RealmConfigurationOptions.builder()
                .name(configuration.get("DEVNT_REALM_NAME"))
                .displayName(configuration.get("DEVNT_REALM_DISPLAY_NAME"))
                .email(Optional.ofNullable(configuration.get("DEVNT_REALM_EMAIL_HOST"))
                        .filter(x -> !x.isEmpty())
                        .map(x -> RealmEmailOptions.builder()
                                .host(configuration.get("DEVNT_REALM_EMAIL_HOST"))
                                .port(Integer.parseInt(configuration.get("DEVNT_REALM_EMAIL_PORT")))
                                .ssl(Boolean.parseBoolean(configuration.get("DEVNT_REALM_EMAIL_SSL")))
                                .startTls(Boolean.parseBoolean(configuration.get("DEVNT_REALM_EMAIL_START_TLS")))
                                .user(configuration.get("DEVNT_REALM_EMAIL_USERNAME"))
                                .password(configuration.get("DEVNT_REALM_EMAIL_PASSWORD"))
                                .from(configuration.get("DEVNT_REALM_EMAIL_FROM"))
                                .build())
                        .orElse(null))
                .build();

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

        var realmOptionsValidator = new RealmOptionsValidator();
        var clientConfigurationOptionsValidator = new ClientOptionsValidator();
        try {
            realmOptionsValidator.validate(realmOptions).throwIfInvalid();
            clientConfigurationOptionsValidator.validate(iotClientOptions).throwIfInvalid();
            clientConfigurationOptionsValidator.validate(nextCloudClientOptions).throwIfInvalid();
        } catch (ValidationException e) {
            logger.error("Error validating client configuration options", e);

            throw e;
        }

        var adOptions = DevntActiveDirectoryConfigurationOptions.builder()
                .realmName(realmOptions.getName())
                .id(configuration.get("DEVNT_AD_ID"))
                .name(configuration.get("DEVNT_AD_NAME"))
                .host(configuration.get("DEVNT_AD_HOST"))
                .bindDN(configuration.get("DEVNT_AD_BIND_DN"))
                .bindCredentials(configuration.get("DEVNT_AD_BIND_CREDENTIALS"))
                .usersDN(configuration.get("DEVNT_AD_USERS_DN"))
                .usersFilter(configuration.get("DEVNT_AD_USERS_FILTER"))
                .attributeMappers(List.of(
                        DevntActiveDirectoryGroupMapperOptions.builder()
                                .id(configuration.get("DEVNT_AD_GROUP_MAPPER_ID"))
                                .name(configuration.get("DEVNT_AD_GROUP_MAPPER_NAME"))
                                .groupsDN(configuration.get("DEVNT_AD_GROUP_MAPPER_DN"))
                                .groupsFilter(configuration.get("DEVNT_AD_GROUP_MAPPER_FILTER"))
                                .build()
                ))
                .build();

        logger.info(startTemplate.formatted(realmOptions.getName()));

        new RealmConfiguration(realmOptions, keycloak.realms()).configure();
        new ClientConfiguration(iotClientOptions, keycloak.realm(realmOptions.getName()).clients()).configure();
        new ClientConfiguration(nextCloudClientOptions, keycloak.realm(realmOptions.getName()).clients()).configure();
        new DevntActiveDirectoryConfiguration(adOptions, keycloak.realm(realmOptions.getName()).components()).configure();

        logger.info(endTemplate.formatted(realmOptions.getName()));
    }
}