package nl.the_experts.keycloak.configuration.devnt.userFederations;

import jakarta.ws.rs
.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.admin.client.resource.ComponentResource;
import org.keycloak.admin.client.resource.ComponentsResource;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;


@JBossLog
@AllArgsConstructor
public class DevntActiveDirectoryConfiguration {
    private static final String COMPONENT_TYPE = "org.keycloak.storage.UserStorageProvider";

    private static final String IDP_PROVIDERID = "ldap";

    private DevntActiveDirectoryConfigurationOptions options;
    private ComponentsResource _componentsResource;

    public void configure() {
        var identityProviders = _componentsResource.query(options.getRealmName(), COMPONENT_TYPE);

        if (identityProviders.stream().noneMatch(x -> x.getId().equals(options.getId()))) {
            createIdentityProvider(this._componentsResource, options);
        }

        updateIdentityProvider(this._componentsResource, options);
    }

    private static void createIdentityProvider(ComponentsResource componentsResource, DevntActiveDirectoryConfigurationOptions options) {

        var representation = new ComponentRepresentation();

        representation.setParentId(options.getRealmName());
        representation.setProviderType(COMPONENT_TYPE);
        representation.setProviderId(IDP_PROVIDERID);
        representation.setId(options.getId());
        representation.setName(options.getName());

        var config = new MultivaluedHashMap<String, String>();
        config.add("enabled", Boolean.toString(false));
        config.add("editMode", "READ_ONLY");

        representation.setConfig(config);

        var response = componentsResource.add(representation);

        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {

            log.error(String.format("Request URI: %s\n" +
                            "Response Status: %s\n",
                            "Response Body: %s",
                    response.getLocation().toString(),
                    response.getStatusInfo().getFamily().toString(),
                    response.hasEntity() ? response.readEntity(String.class) : ""));

            throw new RuntimeException(String.format("Could not create component '%s' of type `%s`", options.getId(), COMPONENT_TYPE));
        }
    }

    private static void updateIdentityProvider(ComponentsResource componentsResource, DevntActiveDirectoryConfigurationOptions options) {

        var resource = componentsResource.component(options.getId());

        var representation = resource.toRepresentation();

        var config = representation.getConfig();

        replace(config, "editMode", "READ_ONLY");
        replace(config, "importEnabled", Boolean.toString(true));
        replace(config, "enabled", Boolean.toString(true));

        replace(config, "connectionUrl", options.getHost());
        replace(config, "vendor", "ad");
        replace(config, "bindDn", options.getBindDN());
        replace(config, "bindCredential", options.getBindCredentials());
        replace(config, "usersDn", options.getUsersDN());
        replace(config, "usernameLDAPAttribute", "sAMAccountName");
        replace(config, "uuidLDAPAttribute", "objectGUID");
        replace(config, "rdnLDAPAttribute", "cn");
        replace(config, "userObjectClasses", "person, organizationalPerson, user");
        replace(config, "trustEmail", Boolean.toString(true));

        representation.setConfig(config);

        resource.update(representation);

        fixAttributeMapping(componentsResource, resource, "username", "sAMAccountName");
        fixAttributeMapping(componentsResource, resource, "firstName", "givenName");
    }

    private static void fixAttributeMapping(ComponentsResource componentsResource, ComponentResource resource, String userModelAttribute, String ldapAttribute) {

        var attributeMappers = componentsResource.query(resource.toRepresentation().getId(), "org.keycloak.storage.ldap.mappers.LDAPStorageMapper");

        var attributeMappingRepresentation = attributeMappers.stream()
                .filter(x -> x.getProviderId().equals("user-attribute-ldap-mapper"))
                .filter(x -> x.getConfig().containsKey("user.model.attribute"))
                .filter(x -> x.getConfig().get("user.model.attribute").stream().findFirst().orElse("").equals(userModelAttribute))
                .findFirst()
                .orElse(null);

        if (attributeMappingRepresentation != null) {

            var cfg = attributeMappingRepresentation.getConfig();

            replace(cfg, "ldap.attribute", ldapAttribute);

            attributeMappingRepresentation.setConfig(cfg);

            var attributeMappingResource = componentsResource.component(attributeMappingRepresentation.getId());

            attributeMappingResource.update(attributeMappingRepresentation);
        }
    }

    private static void replace(MultivaluedHashMap<String, String> map, String key, String value) {
        map.remove(key);
        map.add(key, value);
    }
}
