package nl.the_experts.keycloak.configuration.devnt.clients;

import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.representations.idm.ClientRepresentation;

import java.util.Arrays;

@JBossLog
@AllArgsConstructor
public class IoTClientConfiguration {

    private final IoTClientConfigurationOptions options;
    private final ClientsResource resource;

    public void configure() {
        var clients = resource.findAll();

        if (clients.isEmpty() || clients.stream().noneMatch(client -> client.getId().equals(options.getId()))) {
            createClient(options.getId(), options.getName());
        }

        updateClient(options.getId(), options.getAuthType(), options.getClientSECRET(), options.getRedirectUris());
    }


    private void createClient(String id, String name) {
        var representation = new ClientRepresentation();

        representation.setId(id);
        representation.setClientId(id);
        representation.setName(name);
        representation.setEnabled(false);

        var response = resource.create(representation);

        if(response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException(String.format("Could not create client '%s'", id));
        }

        log.infof("Created client '%s'", id);
    }


    private void updateClient(String clientId, String authenticatorType, String clientSecret, String redirectUris) {

        var representation = new ClientRepresentation();

        representation.setClientAuthenticatorType(authenticatorType);
        representation.setSecret(clientSecret);
        representation.setRedirectUris(Arrays.stream(redirectUris.split(",")).toList());
        representation.setEnabled(true);

        var resource = this.resource.get(clientId);

        resource.update(representation);

    }
}