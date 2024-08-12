package nl.the_experts.keycloak.configuration.devnt.realm;

import nl.the_experts.keycloak.configuration.devnt.OptionsValidator;
import nl.the_experts.keycloak.validation.ValidationResult;

public class EmailOptionsValidator implements OptionsValidator<RealmEmailOptions> {

    @Override
    public ValidationResult validate(RealmEmailOptions options) {
        return ValidationResult.create()
                .build();
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
