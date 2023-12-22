package nl.the_experts.keycloak.configuration;

import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import nl.the_experts.keycloak.configuration.devnt.DevntConfiguration;
import org.keycloak.admin.client.Keycloak;

/**
 * Class to configure Keycloak.
 */
@JBossLog
@AllArgsConstructor
public class KeycloakConfiguration {

    private final Keycloak keycloak;

    /**
     * Starts configuration of Keycloak.
     */
    public void configure() {
        log.info("-----------------------------------------------");
        log.info("Starting Java configuration");
        log.info("-----------------------------------------------");

        new DevntConfiguration(keycloak).configure();

        log.info("-----------------------------------------------");
        log.infof("Finished Java configuration without errors.");
        log.info("-----------------------------------------------");
    }
}
