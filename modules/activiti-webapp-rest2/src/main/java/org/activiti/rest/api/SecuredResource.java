package org.activiti.rest.api;

import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.rest.application.ActivitiRestApplication;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

public class SecuredResource extends ServerResource {

  protected static final String USER = "user";
  protected static final String ADMIN = "admin";
  
  protected boolean authenticate() {
    return authenticate(null);
  }
  
  protected boolean authenticate(String group) {
    String user = ((ActivitiRestApplication) getApplication()).authenticate(getRequest(), getResponse());
    if(user == null) {
      // Not authenticated
      setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Authentication is required");
      return false;
    
    } else if(group == null) {
      return true;
    
    } else {
      boolean allowed = false;
      List<Group> groupList = ActivitiUtil.getIdentityService().createGroupQuery().groupMember(user).list();
      if(groupList != null) {
        for (Group groupObject : groupList) {
          if(groupObject.getId().equals(group)) {
            allowed = true;
            break;
          }
        }
      }
      if(allowed == false) {
        setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User is not part of the group " + group);
      }
      return allowed;
    }
  }

}
