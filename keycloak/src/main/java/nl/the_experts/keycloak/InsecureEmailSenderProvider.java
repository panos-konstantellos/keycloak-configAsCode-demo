package nl.the_experts.keycloak;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.jboss.logging.Logger;
import org.keycloak.common.enums.HostnameVerificationPolicy;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.truststore.FileTruststoreProvider;
import org.keycloak.truststore.JSSETruststoreConfigurator;
import org.keycloak.truststore.TruststoreProvider;
import org.keycloak.truststore.TruststoreProviderFactory;
import org.keycloak.vault.VaultStringSecret;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static org.keycloak.utils.StringUtil.isNotBlank;

/**
 * Based on <a href="https://github.com/keycloak/keycloak/blob/main/services/src/main/java/org/keycloak/email/DefaultEmailSenderProvider.java">DefaultEmailSenderProvider</a>
 */
final class InsecureEmailSenderProvider implements EmailSenderProvider {
    private static final Logger logger = Logger.getLogger(InsecureEmailSenderProvider.class);
    private static final String SUPPORTED_SSL_PROTOCOLS = getSupportedSslProtocols();

    private final KeycloakSession session;

    InsecureEmailSenderProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void send(Map<String, String> config, UserModel user, String subject, String textBody, String htmlBody) throws EmailException {
        send(config, retrieveEmailAddress(user), subject, textBody, htmlBody);
    }

    @Override
    public void send(Map<String, String> config, String address, String subject, String textBody, String htmlBody) throws EmailException {
        Transport transport = null;
        try {

            Properties props = new Properties();

            if (config.containsKey("host")) {
                props.setProperty("mail.smtp.host", config.get("host"));
            }

            boolean auth = "true".equals(config.get("auth"));
            boolean ssl = "true".equals(config.get("ssl"));
            boolean starttls = "true".equals(config.get("starttls"));

            if (config.containsKey("port") && config.get("port") != null) {
                props.setProperty("mail.smtp.port", config.get("port"));
            }

            if (auth) {
                props.setProperty("mail.smtp.auth", "true");
            }

            if (ssl) {
                props.setProperty("mail.smtp.ssl.enable", "true");
            }

            if (starttls) {
                props.setProperty("mail.smtp.starttls.enable", "true");
            }

            if (ssl || starttls || auth) {
                props.put("mail.smtp.ssl.protocols", SUPPORTED_SSL_PROTOCOLS);

                setupTruststore(props, session);
            }

            props.setProperty("mail.smtp.timeout", "10000");
            props.setProperty("mail.smtp.connectiontimeout", "10000");

            String from = config.get("from");
            String fromDisplayName = config.get("fromDisplayName");
            String replyTo = config.get("replyTo");
            String replyToDisplayName = config.get("replyToDisplayName");
            String envelopeFrom = config.get("envelopeFrom");

            Session session = Session.getInstance(props);

            Multipart multipart = new MimeMultipart("alternative");

            if (textBody != null) {
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(textBody, "UTF-8");
                multipart.addBodyPart(textPart);
            }

            if (htmlBody != null) {
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
                multipart.addBodyPart(htmlPart);
            }

            Message msg = new MimeMessage(session);
            msg.setFrom(toInternetAddress(from, fromDisplayName));

            msg.setReplyTo(new Address[]{toInternetAddress(from, fromDisplayName)});

            if (isNotBlank(replyTo)) {
                msg.setReplyTo(new Address[]{toInternetAddress(replyTo, replyToDisplayName)});
            }

            if (isNotBlank(envelopeFrom)) {
                props.setProperty("mail.smtp.from", envelopeFrom);
            }

            msg.setHeader("To", address);
            msg.setSubject(MimeUtility.encodeText(subject, StandardCharsets.UTF_8.name(), null));
            msg.setContent(multipart);
            msg.saveChanges();
            msg.setSentDate(new Date());

            transport = session.getTransport("smtp");
            if (auth) {
                try (VaultStringSecret vaultStringSecret = this.session.vault().getStringSecret(config.get("password"))) {
                    transport.connect(config.get("user"), vaultStringSecret.get().orElse(config.get("password")));
                }
            } else {
                transport.connect();
            }
            transport.sendMessage(msg, new InternetAddress[]{new InternetAddress(address)});
        } catch (Exception e) {
            ServicesLogger.LOGGER.failedToSendEmail(e);
            throw new EmailException(e);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    logger.warn("Failed to close transport", e);
                }
            }
        }
    }

    @Override
    public void close() {

    }

    private static String retrieveEmailAddress(UserModel user) {
        return user.getEmail();
    }

    private static String getSupportedSslProtocols() {
        try {
            String[] protocols = SSLContext.getDefault().getSupportedSSLParameters().getProtocols();
            if (protocols != null) {
                return String.join(" ", protocols);
            }
        } catch (Exception e) {
            logger.warn("Failed to get list of supported SSL protocols", e);
        }
        return null;
    }

    private static JSSETruststoreConfigurator getTruststoreConfigurator(KeycloakSession session) {
        KeycloakSessionFactory factory = session.getKeycloakSessionFactory();
        TruststoreProviderFactory truststoreFactory = (TruststoreProviderFactory) factory.getProviderFactory(TruststoreProvider.class, "file");

        var provider = truststoreFactory.create(session);

        var acceptAnyProvider = new FileTruststoreProvider(
                provider.getTruststore(),
                HostnameVerificationPolicy.ANY,
                provider.getRootCertificates(),
                provider.getIntermediateCertificates());

        return new JSSETruststoreConfigurator(acceptAnyProvider);
    }

    private static void setupTruststore(Properties props, KeycloakSession session) {
        var configurator = getTruststoreConfigurator(session);

        SSLSocketFactory factory = configurator.getSSLSocketFactory();
        if (factory != null) {
            props.put("mail.smtp.ssl.socketFactory", factory);
            if (configurator.getProvider().getPolicy() == HostnameVerificationPolicy.ANY) {
                props.setProperty("mail.smtp.ssl.trust", "*");
                props.put("mail.smtp.ssl.checkserveridentity", Boolean.FALSE.toString());
            }
        }
    }

    private static InternetAddress toInternetAddress(String email, String displayName) throws UnsupportedEncodingException, AddressException, EmailException {
        if (email == null || email.trim().isEmpty()) {
            throw new EmailException("Please provide a valid address", null);
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            return new InternetAddress(email);
        }
        return new InternetAddress(email, displayName, "utf-8");
    }
}