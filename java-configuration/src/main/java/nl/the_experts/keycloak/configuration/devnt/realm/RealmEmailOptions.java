package nl.the_experts.keycloak.configuration.devnt.realm;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RealmEmailOptions {
    private final String host;
    private final int port;
    private final boolean ssl;
    private final boolean startTls;

    private final String user;
    private final String password;

    private final String from;
}
