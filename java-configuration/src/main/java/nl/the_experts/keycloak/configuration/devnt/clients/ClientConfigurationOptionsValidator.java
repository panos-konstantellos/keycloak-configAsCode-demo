package nl.the_experts.keycloak.configuration.devnt.clients;

import com.google.common.base.Strings;

public final class ClientConfigurationOptionsValidator {

    public boolean validate(ClientConfigurationOptions options) throws Exception {
        if (Strings.isNullOrEmpty(options.getId())) {
            throw new Exception("Client ID is required.");
        }

        if (Strings.isNullOrEmpty(options.getName())) {
            throw new Exception("Client name is required.");
        }

        if (Strings.isNullOrEmpty(options.getAuthType())) {
            throw new Exception("Client authenticator type is required.");
        }

        if (Strings.isNullOrEmpty(options.getClientSECRET())) {
            throw new Exception("Client secret is required.");
        }

        if (Strings.isNullOrEmpty(options.getRedirectUris())) {
            throw new Exception("Client redirect URIs are required.");
        }

        return true;
    }
}
