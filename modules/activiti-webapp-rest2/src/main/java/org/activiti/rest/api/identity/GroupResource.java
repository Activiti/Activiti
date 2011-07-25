package org.activiti.rest.api.identity;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

public class GroupResource extends SecuredResource {
  
  @Get
  public Group getGroup() {
    if(authenticate() == false) return null;
    
    String groupId = (String) getRequest().getAttributes().get("groupId");
    if(groupId == null) {
      throw new ActivitiException("No groupId provided");
    }
    Group group = ActivitiUtil.getIdentityService().createGroupQuery().groupId(groupId).singleResult();
    return group;
  }

}
