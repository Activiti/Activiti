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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.exception.ActivitiConflictException;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class GroupCollectionResource {

  protected static HashMap<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  static {
    properties.put("id", GroupQueryProperty.GROUP_ID);
    properties.put("name", GroupQueryProperty.NAME);
    properties.put("type", GroupQueryProperty.TYPE);
  }
  
  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected IdentityService identityService;
  
  @RequestMapping(value="/identity/groups", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getGroups(@RequestParam Map<String,String> allRequestParams, HttpServletRequest request) {
    GroupQuery query = identityService.createGroupQuery();
    
    if (allRequestParams.containsKey("id")) {
      query.groupId(allRequestParams.get("id"));
    }
    if (allRequestParams.containsKey("name")) {
      query.groupName(allRequestParams.get("name"));
    }
    if (allRequestParams.containsKey("nameLike")) {
      query.groupNameLike(allRequestParams.get("nameLike"));
    }
    if (allRequestParams.containsKey("type")) {
      query.groupType(allRequestParams.get("type"));
    }
    if (allRequestParams.containsKey("member")) {
      query.groupMember(allRequestParams.get("member"));
    }
    if (allRequestParams.containsKey("potentialStarter")) {
      query.potentialStarter(allRequestParams.get("potentialStarter"));
    }

    return new GroupPaginateList(restResponseFactory)
        .paginateList(allRequestParams, query, "id", properties);
  }
  
  @RequestMapping(value="/identity/groups", method = RequestMethod.POST, produces = "application/json")
  public GroupResponse createGroup(@RequestBody GroupRequest groupRequest, HttpServletRequest httpRequest, HttpServletResponse response) {
    if (groupRequest.getId() == null) {
      throw new ActivitiIllegalArgumentException("Id cannot be null.");
    }

    // Check if a user with the given ID already exists so we return a CONFLICT
    if (identityService.createGroupQuery().groupId(groupRequest.getId()).count() > 0) {
      throw new ActivitiConflictException("A group with id '" + groupRequest.getId() + "' already exists.");
    }
    
    Group created = identityService.newGroup(groupRequest.getId());
    created.setId(groupRequest.getId());
    created.setName(groupRequest.getName());
    created.setType(groupRequest.getType());
    identityService.saveGroup(created);
    
    response.setStatus(HttpStatus.CREATED.value());
    
    return restResponseFactory.createGroupResponse(created);
  }
  
}
