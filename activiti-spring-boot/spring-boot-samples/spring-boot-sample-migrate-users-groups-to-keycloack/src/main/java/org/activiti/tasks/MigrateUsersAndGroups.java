package org.activiti.tasks;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

@Component
public class MigrateUsersAndGroups {

    @Autowired
    private KeycloakUtil keycloakUtil;

    @Autowired
    private IdentityService identityService;

    public void migrate() {

        List<Group> groups = identityService.createGroupQuery().list();
        for (Group group : groups) {
            keycloakUtil.createGroup(group.getId(), group.getName());
        }

        List<User> users = identityService.createUserQuery().list();
        for (User user : users) {

            List<Group> userGroups = identityService.createGroupQuery().groupMember(user.getId()).list();
            List<String> groupsIds = new ArrayList<>();
            for (Group userGroup : userGroups) {
                groupsIds.add(keycloakUtil.getGroupCreatedId(userGroup.getId()));

            }

            keycloakUtil.createUserWithGroups(user.getId(),
                                              user.getFirstName(),
                                              user.getLastName(),
                                              user.getPassword(),
                                              groupsIds);
        }

    }

    public KeycloakUtil getKeycloakUtil() {
        return keycloakUtil;
    }

    public void setKeycloakUtil(KeycloakUtil keycloakUtil) {
        this.keycloakUtil = keycloakUtil;
    }

}
