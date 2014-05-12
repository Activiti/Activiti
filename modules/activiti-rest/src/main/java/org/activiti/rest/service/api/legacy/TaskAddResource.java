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

package org.activiti.rest.service.api.legacy;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.RequestUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.legacy.task.LegacyTaskResponse;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
public class TaskAddResource extends SecuredResource {
  
  @Put
  public LegacyTaskResponse addTask(Representation entity) {
    try {
      if(authenticate() == false) return null;
      
      String taskParams = entity.getText();
      JsonNode taskJSON = new ObjectMapper().readTree(taskParams);
      
      String name = taskJSON.path("taskName").textValue();
      
      String description = null;
      if(taskJSON.path("description") != null) {
        description = taskJSON.path("description").textValue();
      }
      
      String assignee = null;
      if(taskJSON.path("assignee") != null) {
        assignee = taskJSON.path("assignee").textValue();
      }
      
      String owner = null;
      if(taskJSON.path("owner") != null) {
        owner = taskJSON.path("owner").textValue();
      }
      
      String priority = null;
      if(taskJSON.path("priority") != null) {
        priority = taskJSON.path("priority").textValue();
      }
      
      String dueDate = null;
      if(taskJSON.path("dueDate") != null) {
        dueDate = taskJSON.path("dueDate").textValue();
      }
      
      String parentTaskId = null;
      if(taskJSON.path("parentTaskId") != null) {
        parentTaskId = taskJSON.path("parentTaskId").textValue();
      }
      
      Task newTask = ActivitiUtil.getTaskService().newTask();
      newTask.setName(name);
      newTask.setDescription(description);
      newTask.setAssignee(assignee);
      newTask.setOwner(owner);
      if(priority != null) {
        newTask.setPriority(RequestUtil.parseToInteger(priority));
      }
      if(dueDate != null) {
        newTask.setDueDate(RequestUtil.parseToDate(dueDate));
      }
      newTask.setParentTaskId(parentTaskId);
      ActivitiUtil.getTaskService().saveTask(newTask);
      LegacyTaskResponse response = new LegacyTaskResponse(newTask);
      return response;
      
    } catch (Exception e) {
      if(e instanceof ActivitiException) {
        throw (ActivitiException) e;
      }
      throw new ActivitiException("Failed to add new task", e);
    }
  }
}
