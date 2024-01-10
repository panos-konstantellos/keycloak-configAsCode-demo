package nl.the_experts.keycloak.configuration.devnt.clients;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IoTClientConfigurationOptions {
    private final String id;
    private final String name;
    private final String authType;
    private final String clientSECRET;
    private final String redirectUris;
}
