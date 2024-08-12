package nl.the_experts.keycloak.configuration.devnt.realm;

import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
public class RealmConfiguration {
    private static final Logger logger = Logger.getLogger(RealmConfiguration.class);

    private final RealmConfigurationOptions options;
    private RealmsResource realmsResource;

    /**
     * Configures the realm, first validates if the realm exists and if none exists, creates the realm.
     */
    public void configure() {
        var realms = realmsResource.findAll();

        if (realms.isEmpty() || realms.stream().noneMatch(realm -> realm.getId().equals(options.getName()))) {
            logger.infof("Realm does not yet exist, creating for realm: %s", options.getName());
            createRealm(realmsResource);
        }

        updateRealm();
    }

    private void createRealm(RealmsResource realmsResource) {
        var realmRepresentation = new RealmRepresentation();
        realmRepresentation.setDisplayName(options.getDisplayName());
        realmRepresentation.setId(options.getName());
        realmRepresentation.setRealm(options.getName());
        realmRepresentation.setEnabled(false);

        realmsResource.create(realmRepresentation);
        logger.infof("Created realm '%s'", options.getName());
    }

    private void updateRealm() {
        var realmResource = realmsResource.realm(options.getName());

        var realmRepresentation = realmResource.toRepresentation();

        realmRepresentation.setBruteForceProtected(true);
        realmRepresentation.setLoginWithEmailAllowed(false);
        realmRepresentation.setEnabled(true);

        if (options.getEmail() != null) {
            configureSmtp(options.getEmail(), realmRepresentation);
        }

        realmResource.update(realmRepresentation);
    }

    private static void configureSmtp(RealmEmailOptions options, RealmRepresentation realmRepresentation) {
        Objects.requireNonNull(options, "Email options are required");

        var smtpConfig = realmRepresentation.getSmtpServer();

        if (smtpConfig == null) {
            smtpConfig = new HashMap<>();
        }

        smtpConfig.put("host", options.getHost());
        smtpConfig.put("port", String.valueOf(options.getPort()));
        smtpConfig.put("auth", Optional.ofNullable(options.getUser()).map(x -> Boolean.TRUE).orElse(Boolean.FALSE).toString());
        smtpConfig.put("user", options.getUser());
        smtpConfig.put("password", options.getPassword());
        smtpConfig.put("from", options.getFrom());
        smtpConfig.put("ssl", Boolean.valueOf(options.isSsl()).toString());
        smtpConfig.put("starttls", Boolean.valueOf(options.isStartTls()).toString());

        realmRepresentation.setSmtpServer(smtpConfig);
    }
}