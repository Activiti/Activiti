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

/**
 * @author Tijs Rademakers
 */
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
