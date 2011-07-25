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

package org.activiti.rest.api.task;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class TasksSummaryResource extends SecuredResource {
  
  @Get
  public ObjectNode getTasksSummary() {
    if(authenticate() == false) return null;
    
    String user = getQuery().getValues("user");
    if(user == null) {
      throw new ActivitiException("No user provided");
    }
    
    TaskService ts = ActivitiUtil.getTaskService();
    
    GroupQuery query = ActivitiUtil.getIdentityService()
      .createGroupQuery()
      .groupMember(user)
      .groupType("assignment");
    
    List<Group> groups = query.list();
    ObjectNode groupsJSON = new ObjectMapper().createObjectNode();
    for (Group group : groups) {
      long tasksInGroup = ts.createTaskQuery().taskCandidateGroup(group.getId()).count();
      groupsJSON.put(group.getName(), tasksInGroup);
    }
    
    ObjectNode summaryResponseJSON = new ObjectMapper().createObjectNode();
    
    ObjectNode totalAssignedJSON = new ObjectMapper().createObjectNode();
    totalAssignedJSON.put("total", ts.createTaskQuery().taskAssignee(user).count());
    summaryResponseJSON.put("assigned", totalAssignedJSON);
    
    ObjectNode totalUnassignedJSON = new ObjectMapper().createObjectNode();
    totalUnassignedJSON.put("total", ts.createTaskQuery().taskCandidateUser(user).count());
    totalUnassignedJSON.put("groups", groupsJSON);
    summaryResponseJSON.put("unassigned", totalUnassignedJSON);
    
    return summaryResponseJSON;
  }

}
