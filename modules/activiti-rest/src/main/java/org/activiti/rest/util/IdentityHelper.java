package org.activiti.rest.util;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.rest.Config;

import java.util.List;

public class IdentityHelper {

  private ActivitiRequest req;
  private IdentityService identityService;
  private Config config;

  public IdentityHelper(ActivitiRequest req, IdentityService identityService, Config config) {
    this.req = req;
    this.identityService = identityService;
    this.config = config;
  }

  /**
   * Tests if user is in group.
   *
   * @param groupId
   *          The if of the group to test the user against
   * @return true of user is in group
   */
  protected boolean isUserInGroup(String groupId) {
    String userId = req.getCurrentUserId();
    if (userId != null) {
      List<Group> groups = identityService.createGroupQuery().member(userId).list();
      for (Group group : groups) {
        if (groupId.equals(group.getId())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Tests if user has manager role.
   *
   * @return true if the user has manager role
   */
  protected boolean isManager() {
    return isUserInGroup(config.getManagerGroupId());
  }

  /**
   * Tests if user has admin role.
   *
   * @return true if the user has admin role
   */
  protected boolean isAdmin() {
    return isUserInGroup(config.getAdminGroupId());
  }

}
