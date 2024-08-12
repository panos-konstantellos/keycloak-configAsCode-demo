package nl.the_experts.keycloak.configuration.devnt.realm;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RealmConfigurationOptions {
    private final String name;
    private final String displayName;
    private final RealmEmailOptions email;
}

