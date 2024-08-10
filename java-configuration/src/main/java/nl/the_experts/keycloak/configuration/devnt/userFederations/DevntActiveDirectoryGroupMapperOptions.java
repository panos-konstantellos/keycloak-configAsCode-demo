package nl.the_experts.keycloak.configuration.devnt.userFederations;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class DevntActiveDirectoryGroupMapperOptions implements DevntActiveDirectoryMapperOptions {
    private final String id;
    private final String name;

    private final String groupsDN;
    private final String groupsFilter;
}