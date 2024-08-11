package nl.the_experts.keycloak.validation;

import java.util.List;
import java.util.Objects;

class ValidationResultImpl implements ValidationResult {
    private final boolean success;
    private final List<String> errors;

    ValidationResultImpl(List<String> errors) {
        Objects.requireNonNull(errors, "Errors cannot be null.");

        this.errors = errors;
        this.success = errors.isEmpty();
    }

    public boolean success() {
        return success;
    }

    @Override
    public List<String> errors() {
        return errors;
    }
}