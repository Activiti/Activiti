package org.activiti.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.activiti.engine.RuntimeService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakUtil {

    @Value("${keycloak.auth-server-url}")
    private String authServer;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloakadminclientapp}")
    private String keycloakadminclientapp;

    @Value("${keycloakclientuser}")
    private String clientUser;

    @Value("${keycloakclientpassword}")
    private String clientPassword;

    @Autowired
    private RuntimeService runtimeService;

    private Keycloak keycloak;

    private Map<String, String> groupsIds;

    @PostConstruct
    public void init() {

        keycloak = Keycloak.getInstance(authServer,
                                        realm,
                                        clientUser,
                                        clientPassword,
                                        keycloakadminclientapp);
        groupsIds = new HashMap<>();

    }

    public void createRole(String roleID, String roleName) {
        // Create the role
        RoleRepresentation clientRoleRepresentation = new RoleRepresentation();
        clientRoleRepresentation.setName(roleID);
        clientRoleRepresentation.setDescription(roleName);
        clientRoleRepresentation.setClientRole(true);
        keycloak.realm(realm).clients().findByClientId(keycloakadminclientapp).forEach(clientRepresentation -> keycloak
                                                                                                                       .realm(realm)
                                                                                                                       .clients()
                                                                                                                       .get(clientRepresentation.getId())
                                                                                                                       .roles()
                                                                                                                       .create(clientRoleRepresentation));
    }

    public void createGroup(String groupId, String groupName) {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupId);
        Response response = keycloak.realms().realm(realm).groups().add(group);
        groupsIds.put(groupId, getCreatedId(response));
    }

    public int getAllGroupsSize() {
        return keycloak.realms().realm(realm).groups().groups().size();
    }

    public String getGroupCreatedId(String groupName) {
        return groupsIds.get(groupName);
    }

    public void createUserWithRoles(String userName,
                                    String firstName,
                                    String lastName,
                                    String password,
                                    List<String> roles) {

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userName);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCredentials(Arrays.asList(credential));
        user.setEnabled(true);

        Response response = keycloak.realms().realm(realm).users().create(user);
        String userId = getCreatedId(response);

        // Assign role to the user
        assignClientRoles(keycloak.realms().realm(realm), userId, keycloakadminclientapp, roles);

    }

    public void createUserWithGroups(String userName,
                                     String firstName,
                                     String lastName,
                                     String password,
                                     List<String> groups) {

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userName);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCredentials(Arrays.asList(credential));
        user.setEnabled(true);
        Response response = keycloak.realms().realm(realm).users().create(user);
        for (String groupId : groups) {
            keycloak.realms().realm(realm).users().get(getCreatedId(response)).joinGroup(groupId);
        }

    }

    public UserResource getUser(String userId) {
        return keycloak.realms().realm(realm).users().get(userId);
    }

    private String getCreatedId(Response response) {
        URI location = response.getLocation();
        if (!response.getStatusInfo().equals(Response.Status.CREATED)) {
            Response.StatusType statusInfo = response.getStatusInfo();
            throw new WebApplicationException("Create method returned status " + statusInfo.getReasonPhrase() + " (Code: " + statusInfo.getStatusCode() + "); expected status: Created (201)",
                                              response);
        }
        if (location == null) {
            return null;
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static void assignClientRoles(RealmResource realm, String userId, String clientName, List<String> roles) {
        String realmName = realm.toRepresentation().getRealm();
        String clientId = "";
        for (ClientRepresentation clientRepresentation : realm.clients().findAll()) {
            if (clientRepresentation.getClientId().equals(clientName)) {
                clientId = clientRepresentation.getId();
            }
        }

        if (!clientId.isEmpty()) {
            ClientResource clientResource = realm.clients().get(clientId);

            List<RoleRepresentation> roleRepresentations = new ArrayList<>();
            for (String roleName : roles) {
                RoleRepresentation role = clientResource.roles().get(roleName).toRepresentation();
                roleRepresentations.add(role);
            }

            UserResource userResource = realm.users().get(userId);

            userResource.roles().clientLevel(clientId).add(roleRepresentations);
        }
    }

    public void starMigration() {
        runtimeService.startProcessInstanceByKey("migration");
    }

    public Keycloak getKeycloak() {
        return keycloak;
    }

    public void setKeycloak(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public Response createUser(String username) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        Response response = keycloak.realms().realm(realm).users().create(user);
        return response;
    }

}
