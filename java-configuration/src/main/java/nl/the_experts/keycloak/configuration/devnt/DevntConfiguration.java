package nl.the_experts.keycloak.configuration.devnt;

import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import nl.the_experts.keycloak.configuration.devnt.clients.IoTClientConfiguration;
import org.keycloak.admin.client.Keycloak;

@JBossLog
@AllArgsConstructor
public class DevntConfiguration {
    static final String REALM_NAME = "devnt";
    static final String REALM_DISPLAY_NAME = "devnt.gr";

    private final Keycloak keycloak;
    /**
     * Configures the example realm.
     */
    public void configure() {
        log.info("-----------------------------------------------");
        log.infof("Starting configuration of realm '%s'.", REALM_NAME);
        log.info("-----------------------------------------------");

        new RealmConfiguration(keycloak.realms()).configure(REALM_NAME, REALM_DISPLAY_NAME);
        new IoTClientConfiguration(keycloak.realm(REALM_NAME).clients()).configure();

        log.info("-----------------------------------------------");
        log.infof("Finished configuration of realm '%s'.%n", REALM_NAME);
        log.info("-----------------------------------------------");
    }
}
