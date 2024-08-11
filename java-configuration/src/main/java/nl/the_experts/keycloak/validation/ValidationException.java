package nl.the_experts.keycloak.validation;

import java.util.List;
import java.util.stream.Collectors;

public final class ValidationException extends RuntimeException {
    private static final String messageFormat = """
            Validation Error:
            %s
            """;

    private List<String> errors;

    ValidationException(List<String> errors) {
        super(messageFormat.formatted(buildMessageString(errors)));
    }

    private static String buildMessageString(List<String> errors) {
        return errors.stream().collect(Collectors.joining(System.lineSeparator()));
    }
}