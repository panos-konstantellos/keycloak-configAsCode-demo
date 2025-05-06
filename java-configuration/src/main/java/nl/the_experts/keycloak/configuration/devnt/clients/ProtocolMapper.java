package nl.the_experts.keycloak.configuration.devnt.clients;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ProtocolMapper {
    private final String name;
    private final String protocol;
    private final String protocolMapper;
    private final Map<String, String> config;
}
