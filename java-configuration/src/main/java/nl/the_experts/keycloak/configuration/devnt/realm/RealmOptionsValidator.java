package nl.the_experts.keycloak.configuration.devnt.realm;

import nl.the_experts.keycloak.configuration.devnt.OptionsValidator;
import nl.the_experts.keycloak.validation.ValidationResult;

import java.util.Optional;

public class RealmOptionsValidator implements OptionsValidator<RealmConfigurationOptions> {

    @Override
    public ValidationResult validate(RealmConfigurationOptions options) {
        return ValidationResult.create()
                .validate(() -> !isNullOrEmpty(options.getName()), "Realm name is required")
                .validate(() -> !isNullOrEmpty(options.getDisplayName()), "Realm display name is required")
                .validate(() -> Optional.ofNullable(options.getEmail())
                        .map(new EmailOptionsValidator()::validate)
                        .map(ValidationResult::success)
                        .orElse(true), "Email options are invalid")
                .build();
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}

