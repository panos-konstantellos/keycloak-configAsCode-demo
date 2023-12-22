package nl.the_experts.keycloak.configuration.devnt.clients;

import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;

import java.util.List;

@JBossLog
@AllArgsConstructor
public class IoTClientConfiguration {

    private static final String CLIENT_ID = "iot-client";

    private static final String CLIENT_NAME = "IOT client";

//    private RealmResource realm;

    private ClientsResource _clientsResource;

    public void configure() {
        var clients = _clientsResource.findAll();

        if (clients.isEmpty() || clients.stream().noneMatch(client -> client.getId().equals(CLIENT_ID))) {
            createClient(CLIENT_ID, CLIENT_NAME);
        }

        updateClient(CLIENT_ID);
    }


    private void createClient(String clientId, String clientName) {
        var representation = new ClientRepresentation();

        representation.setId(clientId);
        representation.setClientId(clientId);
        representation.setName(clientName);
        representation.setEnabled(false);

        var response = _clientsResource.create(representation);

        if(response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException(String.format("Could not create client '%s'", CLIENT_ID));
        }

        log.infof("Created client '%s'", CLIENT_ID);

//        realm.clients().create(representation);
//        log.infof("Created client '%s' for realm '%s'", CLIENT_ID, realm.toRepresentation().getId());
    }


    private void updateClient(String clientId) {

        var representation = new ClientRepresentation();

        representation.setClientAuthenticatorType("client-secret");
        representation.setSecret("yBdcTkJEcxSDvhakDvjfuvLXJ9rfKEOz");
        representation.setRedirectUris(List.of("*"));
        representation.setEnabled(true);

        var resource = _clientsResource.get(clientId);

        resource.update(representation);

    }
}
