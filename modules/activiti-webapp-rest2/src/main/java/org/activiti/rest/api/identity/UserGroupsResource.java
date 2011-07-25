package org.activiti.rest.api.identity;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.GroupQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.DefaultPaginateList;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

public class UserGroupsResource extends SecuredResource {
  
  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  public UserGroupsResource() {
    properties.put("id", GroupQueryProperty.GROUP_ID);
    properties.put("name", GroupQueryProperty.NAME);
    properties.put("type", GroupQueryProperty.TYPE);
  }
  
  @Get
  public DataResponse getGroups() {
    if(authenticate() == false) return null;
    
    String userId = (String) getRequest().getAttributes().get("userId");
    if(userId == null) {
      throw new ActivitiException("No userId provided");
    }
    
    DataResponse dataResponse = new DefaultPaginateList().paginateList(getQuery(), 
        ActivitiUtil.getIdentityService().createGroupQuery().groupMember(userId), "id", properties);
    return dataResponse;
  }

}
