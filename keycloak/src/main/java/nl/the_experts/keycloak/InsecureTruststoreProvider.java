package nl.the_experts.keycloak;

import org.keycloak.common.enums.HostnameVerificationPolicy;
import org.keycloak.truststore.TruststoreProvider;

import javax.net.ssl.SSLSocketFactory;
import javax.security.auth.x500.X500Principal;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Map;

public class InsecureTruststoreProvider implements TruststoreProvider {
    public InsecureTruststoreProvider() {

    }
    @Override
    public HostnameVerificationPolicy getPolicy() {
        return null;
    }

    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return null;
    }

    @Override
    public KeyStore getTruststore() {
        return null;
    }

    @Override
    public Map<X500Principal, X509Certificate> getRootCertificates() {
        return Map.of();
    }

    @Override
    public Map<X500Principal, X509Certificate> getIntermediateCertificates() {
        return Map.of();
    }

    @Override
    public void close() {

    }
}
