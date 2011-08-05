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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.TaskQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.TaskQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class TasksResource extends SecuredResource {
  
  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  public TasksResource() {
    properties.put("id", TaskQueryProperty.TASK_ID);
    properties.put("name", TaskQueryProperty.NAME);
    properties.put("description", TaskQueryProperty.DESCRIPTION);
    properties.put("priority", TaskQueryProperty.PRIORITY);
    properties.put("assignee", TaskQueryProperty.ASSIGNEE);
    properties.put("executionId", TaskQueryProperty.EXECUTION_ID);
    properties.put("processInstanceId", TaskQueryProperty.PROCESS_INSTANCE_ID);
  }
  
  @Get
  public DataResponse getTasks() {
    if(authenticate() == false) return null;
    
    String personalTaskUserId = (String) getQuery().getValues("assignee");
    String candidateTaskUserId = (String) getQuery().getValues("candidate");
    String candidateGroupId = (String) getQuery().getValues("candidate-group");
    TaskQuery taskQuery = ActivitiUtil.getTaskService().createTaskQuery();
    if (personalTaskUserId != null) {
      taskQuery.taskAssignee(personalTaskUserId);
    } else if (candidateTaskUserId != null) {
      taskQuery.taskCandidateUser(candidateTaskUserId);
    } else if (candidateGroupId != null) {
      taskQuery.taskCandidateGroup(candidateGroupId);
    } else {
      throw new ActivitiException("Tasks must be filtered with 'assignee', 'candidate' or 'candidate-group'");
    }
    
    DataResponse dataResponse = new TasksPaginateList().paginateList(getQuery(), taskQuery, "id", properties);
    return dataResponse;
  }

}
