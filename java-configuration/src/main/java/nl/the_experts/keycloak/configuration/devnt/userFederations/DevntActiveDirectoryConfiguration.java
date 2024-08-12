package nl.the_experts.keycloak.configuration.devnt.userFederations;

import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import nl.the_experts.keycloak.configuration.devnt.clients.ClientConfiguration;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.resource.ComponentResource;
import org.keycloak.admin.client.resource.ComponentsResource;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.LDAPConstants;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.storage.ldap.mappers.membership.group.GroupLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.membership.group.GroupMapperConfig;

import java.util.Optional;

@AllArgsConstructor
public class DevntActiveDirectoryConfiguration {
    private static final String COMPONENT_TYPE = "org.keycloak.storage.UserStorageProvider";
    private static final String IDP_PROVIDERID = "ldap";
    private static final Logger logger = Logger.getLogger(DevntActiveDirectoryConfiguration.class);

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
            logger.error(String.format("Request URI: %s\n" +
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


        replace(config, LDAPConstants.EDIT_MODE, "READ_ONLY");
        replace(config, "importEnabled", Boolean.toString(true));
        replace(config, LDAPConstants.ENABLED, Boolean.toString(true));

        replace(config, LDAPConstants.CONNECTION_URL, options.getHost());
        replace(config, LDAPConstants.VENDOR, "ad");
        replace(config, LDAPConstants.BIND_DN, options.getBindDN());
        replace(config, LDAPConstants.BIND_CREDENTIAL, options.getBindCredentials());
        replace(config, LDAPConstants.USERS_DN, options.getUsersDN());
        replace(config, LDAPConstants.CUSTOM_USER_SEARCH_FILTER, options.getUsersFilter());
        replace(config, LDAPConstants.USERNAME_LDAP_ATTRIBUTE, "sAMAccountName");
        replace(config, LDAPConstants.UUID_LDAP_ATTRIBUTE, "objectGUID");
        replace(config, LDAPConstants.RDN_LDAP_ATTRIBUTE, "cn");
        replace(config, LDAPConstants.USER_OBJECT_CLASSES, "person, organizationalPerson, user");
        replace(config, LDAPConstants.TRUST_EMAIL, Boolean.toString(true));
        replace(config, "importEnabled", Boolean.toString(true));
        replace(config, LDAPConstants.SYNC_REGISTRATIONS, Boolean.toString(false));

        representation.setConfig(config);

        resource.update(representation);

        fixAttributeMapping(componentsResource, resource, "username", "sAMAccountName");
        fixAttributeMapping(componentsResource, resource, "firstName", "givenName");
        fixAttributeMapping(componentsResource, resource, "lastName", "sn");

        for (var mapperOptions : options.getAttributeMappers()) {
            if (mapperOptions instanceof DevntActiveDirectoryGroupMapperOptions groupMapperOptions) {
                var groupMappers = componentsResource.query(options.getId(), "org.keycloak.storage.ldap.mappers.LDAPStorageMapper");
                if (groupMappers.stream().noneMatch(x -> x.getId().equals(groupMapperOptions.getId()))) {
                    createGroupLdapMapper(componentsResource, options.getId(), groupMapperOptions);
                }

                updateGroupLdapMapper(componentsResource, groupMapperOptions);
            }
        }
    }

    private static void createGroupLdapMapper(ComponentsResource componentsResource, String parentId, DevntActiveDirectoryGroupMapperOptions options) {
        var representation = new ComponentRepresentation();

        representation.setId(options.getId());
        representation.setParentId(parentId);
        representation.setProviderType("org.keycloak.storage.ldap.mappers.LDAPStorageMapper");
        representation.setProviderId(GroupLDAPStorageMapperFactory.PROVIDER_ID);
        representation.setName(options.getName());

        var config = new MultivaluedHashMap<String, String>();

        config.add("groups.dn", options.getGroupsDN());
        config.add("mode", "READ_ONLY");

        representation.setConfig(config);

        var response = componentsResource.add(representation);

        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            logger.error(String.format("""
                            Request URI: %s
                            Response Status: %s
                            Response Body: %s""",
                    Optional.ofNullable(response.getLocation()).map(Object::toString).orElse(""),
                    response.getStatusInfo().getFamily().toString(),
                    response.hasEntity() ? response.readEntity(String.class) : ""));

            throw new RuntimeException(String.format("Could not create group-ldap-mapper %s for '%s'", options.getName(), parentId));
        }
    }

    private static void updateGroupLdapMapper(ComponentsResource componentsResource, DevntActiveDirectoryGroupMapperOptions options) {
        var resource = componentsResource.component(options.getId());

        var representation = resource.toRepresentation();

        representation.setProviderType("org.keycloak.storage.ldap.mappers.LDAPStorageMapper");
        representation.setProviderId(GroupLDAPStorageMapperFactory.PROVIDER_ID);
        representation.setName(options.getName());

        var config = representation.getConfig();

        replace(config, GroupMapperConfig.GROUPS_DN, options.getGroupsDN());
        replace(config, GroupMapperConfig.GROUP_NAME_LDAP_ATTRIBUTE, "cn");
        replace(config, GroupMapperConfig.GROUP_OBJECT_CLASSES, "group");
        replace(config, GroupMapperConfig.PRESERVE_GROUP_INHERITANCE, Boolean.toString(true));
        replace(config, GroupMapperConfig.IGNORE_MISSING_GROUPS, Boolean.toString(false));
        replace(config, GroupMapperConfig.MEMBERSHIP_LDAP_ATTRIBUTE, "member");
        replace(config, GroupMapperConfig.MEMBERSHIP_ATTRIBUTE_TYPE, "DN");
        replace(config, GroupMapperConfig.MEMBERSHIP_USER_LDAP_ATTRIBUTE, "sAMAccountName");
        replace(config, GroupMapperConfig.GROUPS_LDAP_FILTER, options.getGroupsFilter());
        replace(config, GroupMapperConfig.MODE, "READ_ONLY");
        replace(config, GroupMapperConfig.USER_ROLES_RETRIEVE_STRATEGY, "LOAD_GROUPS_BY_MEMBER_ATTRIBUTE_RECURSIVELY");
        replace(config, GroupMapperConfig.MEMBEROF_LDAP_ATTRIBUTE, "memberOf");
        replace(config, GroupMapperConfig.MAPPED_GROUP_ATTRIBUTES, "");
        replace(config, GroupMapperConfig.DROP_NON_EXISTING_GROUPS_DURING_SYNC, Boolean.toString(false));

        representation.setConfig(config);

        resource.update(representation);
    }

    private static void fixAttributeMapping(ComponentsResource componentsResource, ComponentResource resource, String userModelAttribute, String ldapAttribute) {

        var attributeMappers = componentsResource.query(resource.toRepresentation().getId(), "org.keycloak.storage.ldap.mappers.LDAPStorageMapper");

        var attributeMappingRepresentation = attributeMappers.stream()
                .filter(x -> x.getProviderId().equals("user-attribute-ldap-mapper"))
                .filter(x -> x.getConfig().containsKey("user.model.attribute"))
                .filter(x -> x.getConfig().get("user.model.attribute").stream().findFirst().orElse("").equals(userModelAttribute))
                .findFirst()
                .orElse(null);

        if (attributeMappingRepresentation == null) {
            throw new NullPointerException(String.format("Could not find attribute mapping for user model attribute '%s'", userModelAttribute));
        }

        var cfg = attributeMappingRepresentation.getConfig();

        replace(cfg, "ldap.attribute", ldapAttribute);

        attributeMappingRepresentation.setConfig(cfg);

        var attributeMappingResource = componentsResource.component(attributeMappingRepresentation.getId());

        attributeMappingResource.update(attributeMappingRepresentation);
    }

    private static void replace(MultivaluedHashMap<String, String> map, String key, String value) {
        map.remove(key);

        if (value != null) {
            map.add(key, value);
        }
    }
}