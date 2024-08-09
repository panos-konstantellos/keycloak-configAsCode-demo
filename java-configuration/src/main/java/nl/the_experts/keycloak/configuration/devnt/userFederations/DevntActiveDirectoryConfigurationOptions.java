package nl.the_experts.keycloak.configuration.devnt.userFederations;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DevntActiveDirectoryConfigurationOptions {
    private final String realmName;
    private final String id;
    private final String name;
    private final String host;
    private final String bindDN;
    private final String bindCredentials;
    private final String usersDN;
    private final String usersFilter;
}
