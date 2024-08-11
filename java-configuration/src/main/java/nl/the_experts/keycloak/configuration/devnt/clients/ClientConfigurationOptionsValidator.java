package nl.the_experts.keycloak.configuration.devnt.clients;

import nl.the_experts.keycloak.validation.ValidationResult;

public final class ClientConfigurationOptionsValidator {

    public ValidationResult validate(ClientConfigurationOptions options) throws Exception {
        return ValidationResult.create()
                .validate(() -> isNullOrEmpty(options.getId()), "Client ID is required.")
                .validate(() -> isNullOrEmpty(options.getName()), "Client name is required.")
                .validate(() -> isNullOrEmpty(options.getAuthType()), "Client authenticator type is required.")
                .validate(() -> isNullOrEmpty(options.getClientSECRET()), "Client secret is required.")
                .validate(() -> isNullOrEmpty(options.getRedirectUris()), "Client redirect URIs are required.")
                .build();
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}