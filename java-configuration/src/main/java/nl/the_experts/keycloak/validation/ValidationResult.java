package nl.the_experts.keycloak.validation;

import java.util.List;

public interface ValidationResult {
    boolean success();

    List<String> errors();

    static ValidationResultBuilder create() {
        return new ValidationResultBuilder();
    }

    static ValidationResult ok() {
        return new ValidationResultImpl(List.of());
    }

    static ValidationResult failure(String error) {
        return new ValidationResultImpl(List.of(error));
    }

    static ValidationResult failure(List<String> errors) {
        return new ValidationResultImpl(errors);
    }
}

