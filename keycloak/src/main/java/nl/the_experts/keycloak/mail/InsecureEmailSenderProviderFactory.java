package nl.the_experts.keycloak.mail;

import org.keycloak.Config;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.email.EmailSenderProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class InsecureEmailSenderProviderFactory implements EmailSenderProviderFactory {
    public static final String PROVIDER_ID = "insecure-smtp";

    @Override
    public EmailSenderProvider create(KeycloakSession session) {
        return new InsecureEmailSenderProvider(session);
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory sessionFactory) {
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}