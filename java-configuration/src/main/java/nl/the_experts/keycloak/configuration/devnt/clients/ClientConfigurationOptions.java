package nl.the_experts.keycloak.configuration.devnt.clients;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class ClientConfigurationOptions {
    private final String id;
    private final String name;
    private final String authType;
    private final String clientSECRET;
    private final String redirectUris;
    private final boolean standardFlowEnabled;
    private final boolean serviceAccountsEnabled;
    private final List<ProtocolMapper> mappers;

    @Builder
    public static ClientConfigurationOptions create(
            String id,
            String name,
            String authType,
            String clientSECRET,
            String redirectUris,
            boolean standardFlowEnabled,
            boolean serviceAccountsEnabled,
            List<ProtocolMapper> mappers
    ) {
        return new ClientConfigurationOptions(
                id,
                name,
                authType,
                clientSECRET,
                redirectUris != null ? redirectUris : "",
                standardFlowEnabled,
                serviceAccountsEnabled,
                mappers != null ? mappers : new ArrayList<>()
        );
    }

    public static class ClientConfigurationOptionsBuilder {
        public ClientConfigurationOptionsBuilder() {
            this.standardFlowEnabled = true;
            this.redirectUris = "";
            this.mappers = new ArrayList<>();
        }

        public ClientConfigurationOptionsBuilder addMapper(ProtocolMapper mapper) {
            Objects.requireNonNull(mapper);

            this.mappers.add(mapper);

            return this;
        }
    }
}
