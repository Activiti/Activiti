/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.identity.keycloak;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.UserGroupLookupProxy;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("usergrouplookuproxy")
public class KeycloakUserGroupLookupProxy implements UserGroupLookupProxy {

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

    public List<String> getGroupsForCandidateUser(String candidateUser) {
        //candidateUser here will use identifier chosen in KeycloakActivitiAuthenticationProvider

        Keycloak keycloak = Keycloak.getInstance(authServer,
                                                 realm,
                                                 clientUser,
                                                 clientPassword,
                                                 keycloakadminclientapp);

        //if using id then could use keycloak.realms().realm(realm).users().get(candidateUser)
        //but with name have to search for user
        List<UserRepresentation> users = keycloak.realms().realm(realm).users().search(candidateUser,
                                                                                       0,
                                                                                       10);
        if (users.size() > 1) {
            throw new UnsupportedOperationException("User id " + candidateUser + " is not unique");
        }
        UserRepresentation user = users.get(0);

        List<GroupRepresentation> groupRepresentations = keycloak.realms().realm(realm).users().get(user.getId()).groups();

        List<String> groups = null;
        if (groupRepresentations != null && groupRepresentations.size() > 0) {
            groups = new ArrayList<String>();
            for (GroupRepresentation groupRepresentation : groupRepresentations) {
                groups.add(groupRepresentation.getName());
            }
        }

        //to use roles instead of groups the query would be keycloak.realms().realm(realm).users().get(user.getId()).roles().realmLevel().listEffective()

        return groups;
    }
}
