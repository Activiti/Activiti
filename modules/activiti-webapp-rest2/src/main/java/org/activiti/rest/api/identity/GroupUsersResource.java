package org.activiti.rest.api.identity;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.UserQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

public class GroupUsersResource extends SecuredResource {
  
  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  public GroupUsersResource() {
    properties.put("id", UserQueryProperty.USER_ID);
    properties.put("firstName", UserQueryProperty.FIRST_NAME);
    properties.put("lastName", UserQueryProperty.LAST_NAME);
    properties.put("email", UserQueryProperty.EMAIL);
  }
  
  @Get
  public DataResponse getGroups() {
    if(authenticate() == false) return null;
    
    String groupId = (String) getRequest().getAttributes().get("groupId");
    if(groupId == null) {
      throw new ActivitiException("No groupId provided");
    }
    
    DataResponse dataResponse = new GroupUsersPaginateList().paginateList(getQuery(), 
        ActivitiUtil.getIdentityService().createUserQuery().memberOfGroup(groupId), "id", properties);
    return dataResponse;
  }

}
