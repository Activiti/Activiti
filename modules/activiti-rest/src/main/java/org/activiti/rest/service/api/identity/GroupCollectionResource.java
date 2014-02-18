/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.rest.service.api.identity;

import java.util.HashMap;
import java.util.Set;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class GroupCollectionResource extends SecuredResource {

  protected static HashMap<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  static {
    properties.put("id", GroupQueryProperty.GROUP_ID);
    properties.put("name", GroupQueryProperty.NAME);
    properties.put("type", GroupQueryProperty.TYPE);
  }
  
  @Get
  public DataResponse getGroups() {
    if(!authenticate())
      return null;

    GroupQuery query = ActivitiUtil.getIdentityService().createGroupQuery();
    Form form = getQuery();
    Set<String> names = form.getNames();
    
    if(names.contains("id")) {
      query.groupId(getQueryParameter("id", form));
    }
    if(names.contains("name")) {
      query.groupName(getQueryParameter("name", form));
    }
    if(names.contains("nameLike")) {
      query.groupNameLike(getQueryParameter("nameLike", form));
    }
    if(names.contains("type")) {
      query.groupType(getQueryParameter("type", form));
    }
    if(names.contains("member")) {
      query.groupMember(getQueryParameter("member", form));
    }
    if(names.contains("potentialStarter")) {
      query.potentialStarter(getQueryParameter("potentialStarter", form));
    }

    return new GroupPaginateList(this).paginateList(form, query, "id", properties);
  }
  
  @Post
  public GroupResponse createGroup(GroupRequest request) {
  	if(authenticate() == false) return null;
  	
    if(request.getId() == null) {
      throw new ActivitiIllegalArgumentException("Id cannot be null.");
    }

    // Check if a user with the given ID already exists so we return a CONFLICT
    if(ActivitiUtil.getIdentityService().createGroupQuery().groupId(request.getId()).count() > 0) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT.getCode(), "A group with id '" + request.getId() + "' already exists.", null, null);
    }
    
    Group created = ActivitiUtil.getIdentityService().newGroup(request.getId());
    created.setId(request.getId());
    created.setName(request.getName());
    created.setType(request.getType());
    ActivitiUtil.getIdentityService().saveGroup(created);
    
    setStatus(Status.SUCCESS_CREATED);
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createGroupResponse(this, created);
  }
  
}
