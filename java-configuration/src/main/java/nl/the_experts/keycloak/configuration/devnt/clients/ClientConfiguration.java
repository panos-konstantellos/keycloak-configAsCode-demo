package nl.the_experts.keycloak.configuration.devnt.clients;

import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.ProtocolMappersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public class ClientConfiguration {
    private static final Logger logger = Logger.getLogger(ClientConfiguration.class);

    private final ClientConfigurationOptions options;
    private final ClientsResource resource;

    public void configure() {
        var clients = resource.findAll();

        if (clients.isEmpty() || clients.stream().noneMatch(client -> client.getId().equals(options.getId()))) {
            createClient(options.getId(), options.getName());
        }

        updateClient(options.getId(), options.getAuthType(), options.getClientSECRET(), options.getRedirectUris(), options.isStandardFlowEnabled(), options.isServiceAccountsEnabled(), options.getMappers());
    }


    private void createClient(String id, String name) {
        var representation = new ClientRepresentation();

        representation.setId(id);
        representation.setClientId(id);
        representation.setName(name);
        representation.setEnabled(false);

        var response = resource.create(representation);

        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException(String.format("Could not create client '%s'", id));
        }

        logger.infof("Created client '%s'", id);
    }

    private void updateClient(String clientId, String authenticatorType, String clientSecret, String redirectUris, boolean standardFlowEnabled, boolean serviceAccountsEnabled, List<ProtocolMapper> protocolMappers) {

        var representation = new ClientRepresentation();

        representation.setClientAuthenticatorType(authenticatorType);
        representation.setSecret(clientSecret);
        representation.setRedirectUris(Arrays.stream(redirectUris.split(",")).toList());
        representation.setStandardFlowEnabled(standardFlowEnabled);
        representation.setServiceAccountsEnabled(serviceAccountsEnabled);
        representation.setEnabled(true);

        var resource = this.resource.get(clientId);

        resource.update(representation);

        var protocolMappersResource = resource.getProtocolMappers();

        for (var protocolMapper : protocolMappers) {
            configureMapper(protocolMappersResource, protocolMapper);
        }
    }

    private void configureMapper(ProtocolMappersResource resource, ProtocolMapper protocolMapper) {
        var mappers = resource.getMappers();

        if (mappers.isEmpty() || mappers.stream().noneMatch(x -> x.getName().equalsIgnoreCase(protocolMapper.getName()))) {
            createMapper(resource, protocolMapper.getName(), protocolMapper.getProtocol(), protocolMapper.getProtocolMapper());
        }

        updateMapper(resource, protocolMapper.getName(), protocolMapper.getProtocol(), protocolMapper.getProtocolMapper(), protocolMapper.getConfig());
    }

    private void createMapper(ProtocolMappersResource resource, String name, String protocol, String protocolMapper) {
        Objects.requireNonNull(name);

        var representation = new ProtocolMapperRepresentation();

        representation.setName(name);
        representation.setProtocol(protocol);
        representation.setProtocolMapper(protocolMapper);

        var response = resource.createMapper(representation);

        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new RuntimeException(String.format("Could not create mapper '%s'", name));
        }

        logger.infof("Created mapper '%s'", name);
    }

    private void updateMapper(ProtocolMappersResource resource, String name, String protocol, String protocolMapper, Map<String, String> config) {
        var representation = resource.getMappers()
                .stream()
                .filter(x -> x.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow();

        representation.setProtocol(protocol);
        representation.setProtocolMapper(protocolMapper);

        representation.getConfig().putAll(config);

        resource.update(representation.getId(), representation);
    }
}