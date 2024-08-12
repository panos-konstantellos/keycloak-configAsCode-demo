package nl.the_experts.keycloak.configuration;

import lombok.AllArgsConstructor;
import nl.the_experts.keycloak.configuration.devnt.DevntConfiguration;

/**
 * Class to configure Keycloak.
 */
@AllArgsConstructor
public class KeycloakConfiguration {
    private final KeycloakConfigurationProperties configuration;

    /**
     * Starts configuration of Keycloak.
     */
    public void configure() {
        var keycloak = KeycloakClientBuilder.create(
                        configuration.get("KEYCLOAK_SERVER"),
                        configuration.get("KEYCLOAK_USER"),
                        configuration.get("KEYCLOAK_PASSWORD"),
                        configuration.get("KEYCLOAK_REALM"))
                .getClient();

        new DevntConfiguration(configuration, keycloak).configure();
    }
}