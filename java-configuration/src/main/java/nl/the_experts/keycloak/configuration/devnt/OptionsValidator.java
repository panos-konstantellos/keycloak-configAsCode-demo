package nl.the_experts.keycloak.configuration.devnt;

import nl.the_experts.keycloak.validation.ValidationResult;

public interface OptionsValidator<TOptions> {
    ValidationResult validate(TOptions options);
}