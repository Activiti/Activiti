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

package org.activiti.rest.service.api.runtime.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.RequestUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tijs Rademakers
 */
@RestController
public class TaskCollectionResource extends TaskBaseResource {
 
  @RequestMapping(value="/runtime/tasks", method = RequestMethod.GET, produces="application/json")
  public DataResponse getTasks(@RequestParam Map<String, String> requestParams, HttpServletRequest httpRequest) {
    // Create a Task query request
    TaskQueryRequest request = new TaskQueryRequest();
    
    // Populate filter-parameters
    if (requestParams.containsKey("name")) {
      request.setName(requestParams.get("name"));
    }
    
    if (requestParams.containsKey("nameLike")) {
      request.setNameLike(requestParams.get("nameLike"));
    }
    
    if (requestParams.containsKey("description")) {
      request.setDescription(requestParams.get("description"));
    }
    
    if (requestParams.containsKey("descriptionLike")) {
      request.setDescriptionLike(requestParams.get("descriptionLike"));
    }
    
    if (requestParams.containsKey("priority")) {
      request.setPriority(Integer.valueOf(requestParams.get("priority")));
    }
    
    if (requestParams.containsKey("minimumPriority")) {
      request.setMinimumPriority(Integer.valueOf(requestParams.get("minimumPriority")));
    }
    
    if (requestParams.containsKey("maximumPriority")) {
      request.setMaximumPriority(Integer.valueOf(requestParams.get("maximumPriority")));
    }
    
    if (requestParams.containsKey("assignee")) {
      request.setAssignee(requestParams.get("assignee"));
    }
    
    if (requestParams.containsKey("assigneeLike")) {
      request.setAssigneeLike(requestParams.get("assigneeLike"));
    }
    
    if (requestParams.containsKey("owner")) {
      request.setOwner(requestParams.get("owner"));
    }
    
    if (requestParams.containsKey("ownerLike")) {
      request.setOwnerLike(requestParams.get("ownerLike"));
    }
    
    if (requestParams.containsKey("unassigned")) {
      request.setUnassigned(Boolean.valueOf(requestParams.get("unassigned")));
    }
    
    if (requestParams.containsKey("delegationState")) {
      request.setDelegationState(requestParams.get("delegationState"));
    }
    
    if (requestParams.containsKey("candidateUser")) {
      request.setCandidateUser(requestParams.get("candidateUser"));
    }
    
    if (requestParams.containsKey("involvedUser")) {
      request.setInvolvedUser(requestParams.get("involvedUser"));
    }
    
    if (requestParams.containsKey("candidateGroup")) {
      request.setCandidateGroup(requestParams.get("candidateGroup"));
    }

    if (requestParams.containsKey("candidateGroups")) {
      String[] candidateGroups = requestParams.get("candidateGroups").split(",");
      List<String> groups = new ArrayList<String>(candidateGroups.length);
      for (String candidateGroup : candidateGroups) {
        groups.add(candidateGroup);
      }
      request.setCandidateGroupIn(groups);
    }
    
    if (requestParams.containsKey("processDefinitionId")) {
      request.setProcessDefinitionId(requestParams.get("processDefinitionId"));
    }
    
    if (requestParams.containsKey("processDefinitionKey")) {
      request.setProcessDefinitionKey(requestParams.get("processDefinitionKey"));
    }
    
    if (requestParams.containsKey("processDefinitionKeyLike")) {
    	request.setProcessDefinitionKeyLike(requestParams.get("processDefinitionKeyLike"));
    }
    
    if (requestParams.containsKey("processDefinitionName")) {
    	request.setProcessDefinitionName(requestParams.get("processDefinitionName"));
    }
    
    if (requestParams.containsKey("processDefinitionNameLike")) {
    	request.setProcessDefinitionNameLike(requestParams.get("processDefinitionNameLike"));
    }
    
    if (requestParams.containsKey("processInstanceId")) {
      request.setProcessInstanceId(requestParams.get("processInstanceId"));
    }

    if (requestParams.containsKey("processInstanceIdIn")) {
      String[] processInstanceIds = requestParams.get("processInstanceIdIn").split(",");
      List<String> ids = Arrays.asList(processInstanceIds);
      request.setProcessInstanceIdIn(ids);
    }

    if (requestParams.containsKey("processInstanceBusinessKey")) {
      request.setProcessInstanceBusinessKey(requestParams.get("processInstanceBusinessKey"));
    }
    
    if (requestParams.containsKey("processInstanceBusinessKeyLike")) {
      request.setProcessInstanceBusinessKeyLike(requestParams.get("processInstanceBusinessKeyLike"));
    }
    
    if (requestParams.containsKey("executionId")) {
      request.setExecutionId(requestParams.get("executionId"));
    }
    
    if (requestParams.containsKey("createdOn")) {
      request.setCreatedOn(RequestUtil.getDate(requestParams, "createdOn"));
    }
    
    if (requestParams.containsKey("createdBefore")) {
      request.setCreatedBefore(RequestUtil.getDate(requestParams, "createdBefore"));
    }
    
    if (requestParams.containsKey("createdAfter")) {
      request.setCreatedAfter(RequestUtil.getDate(requestParams, "createdAfter"));
    }
    
    if (requestParams.containsKey("excludeSubTasks")) {
      request.setExcludeSubTasks(Boolean.valueOf(requestParams.get("excludeSubTasks")));
    }
    
    if (requestParams.containsKey("taskDefinitionKey")) {
      request.setTaskDefinitionKey(requestParams.get("taskDefinitionKey"));
    }
    
    if (requestParams.containsKey("taskDefinitionKeyLike")) {
      request.setTaskDefinitionKeyLike(requestParams.get("taskDefinitionKeyLike"));
    }
    
    if (requestParams.containsKey("dueDate")) {
      request.setDueDate(RequestUtil.getDate(requestParams, "dueDate"));
    }
    
    if (requestParams.containsKey("dueBefore")) {
      request.setDueBefore(RequestUtil.getDate(requestParams, "dueBefore"));
    }
    
    if (requestParams.containsKey("dueAfter")) {
      request.setDueAfter(RequestUtil.getDate(requestParams, "dueAfter"));
    }
    
    if (requestParams.containsKey("active")) {
      request.setActive(Boolean.valueOf(requestParams.get("active")));
    }
    
    if (requestParams.containsKey("includeTaskLocalVariables")) {
      request.setIncludeTaskLocalVariables(Boolean.valueOf(requestParams.get("includeTaskLocalVariables")));
    }
    
    if (requestParams.containsKey("includeProcessVariables")) {
      request.setIncludeProcessVariables(Boolean.valueOf(requestParams.get("includeProcessVariables")));
    }
    
    if (requestParams.containsKey("tenantId")) {
      request.setTenantId(requestParams.get("tenantId"));
    }
    
    if (requestParams.containsKey("tenantIdLike")) {
    	request.setTenantIdLike(requestParams.get("tenantIdLike"));
    }
    
    if (requestParams.containsKey("withoutTenantId") && Boolean.valueOf(requestParams.get("withoutTenantId"))) {
    	request.setWithoutTenantId(Boolean.TRUE);
    }

    if (requestParams.containsKey("candidateOrAssigned")) {
      request.setCandidateOrAssigned(requestParams.get("candidateOrAssigned"));
    }
    
    if (requestParams.containsKey("category")) {
      request.setCategory(requestParams.get("category"));
    }

    return getTasksFromQueryRequest(request, requestParams);
  }
  
  @RequestMapping(value="/runtime/tasks", method = RequestMethod.POST, produces="application/json")
  public TaskResponse createTask(@RequestBody TaskRequest taskRequest, 
      HttpServletRequest request, HttpServletResponse response) {
    
    Task task = taskService.newTask();

    // Populate the task properties based on the request
    populateTaskFromRequest(task, taskRequest);
    if (taskRequest.isTenantIdSet()) {
      ((TaskEntity) task).setTenantId(taskRequest.getTenantId());
    }
    taskService.saveTask(task);

    response.setStatus(HttpStatus.CREATED.value());
    return restResponseFactory.createTaskResponse(task);
  }
}
