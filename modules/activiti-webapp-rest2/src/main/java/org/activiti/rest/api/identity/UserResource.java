package org.activiti.rest.api.identity;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

public class UserResource extends SecuredResource {
  
  @Get
  public UserResponse getUser() {
    if(authenticate() == false) return null;
    
    String userId = (String) getRequest().getAttributes().get("userId");
    if(userId == null) {
      throw new ActivitiException("No userId provided");
    }
    User user = ActivitiUtil.getIdentityService().createUserQuery().userId(userId).singleResult();
    UserResponse response = new UserResponse(user);
    return response;
  }

}
