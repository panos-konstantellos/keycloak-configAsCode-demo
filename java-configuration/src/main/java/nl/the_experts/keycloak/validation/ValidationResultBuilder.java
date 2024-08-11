package nl.the_experts.keycloak.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class ValidationResultBuilder {
    private final List<String> errors;

    ValidationResultBuilder() {
        this.errors = new ArrayList<>();
    }

    public ValidationResultBuilder addError(String error) {
        errors.add(error);

        return this;
    }

    public ValidationResultBuilder validate(BooleanSupplier condition, String errorMessage) {
        if (!condition.getAsBoolean()) {
            errors.add(errorMessage);
        }

        return this;
    }

    public ValidationResult build() {
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.failure(errors);
    }
}
