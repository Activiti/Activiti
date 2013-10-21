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

import java.util.Set;

import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class TaskCollectionResource extends TaskBaseResource {

  @Post
  public TaskResponse createTask(TaskRequest taskRequest) {
    if(!authenticate()) { return null; }
    
    if(taskRequest == null) {
      throw new ResourceException(new Status(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(),
              "A request body was expected when creating the task.", null, null));
    }
    
    Task task = ActivitiUtil.getTaskService().newTask();

    // Populate the task properties based on the request
    populateTaskFromRequest(task, taskRequest);
    ActivitiUtil.getTaskService().saveTask(task);

    setStatus(Status.SUCCESS_CREATED);
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createTaskResponse(this, task);
  }
  
  @Get
  public DataResponse getTasks() {
    if(!authenticate()) { return null; }
    
    // Create a Task query request
    TaskQueryRequest request = new TaskQueryRequest();
    Form query = getQuery();
    Set<String> names = query.getNames();
    
    // Populate filter-parameters
    if(names.contains("name")) {
      request.setName(getQueryParameter("name", query));
    }
    
    if(names.contains("nameLike")) {
      request.setNameLike(getQueryParameter("nameLike", query));
    }
    
    if(names.contains("description")) {
      request.setDescription(getQueryParameter("description", query));
    }
    
    if(names.contains("descriptionLike")) {
      request.setDescriptionLike(getQueryParameter("descriptionLike", query));
    }
    
    if(names.contains("priority")) {
      request.setPriority(getQueryParameterAsInt("priority", query));
    }
    
    if(names.contains("minimumPriority")) {
      request.setMinimumPriority(getQueryParameterAsInt("minimumPriority", query));
    }
    
    if(names.contains("maximumPriority")) {
      request.setMaximumPriority(getQueryParameterAsInt("maximumPriority", query));
    }
    
    if(names.contains("assignee")) {
      request.setAssignee(getQueryParameter("assignee", query));
    }
    
    if(names.contains("owner")) {
      request.setOwner(getQueryParameter("owner", query));
    }
    
    if(names.contains("unassigned")) {
      request.setUnassigned(getQueryParameterAsBoolean("unassigned", query));
    }
    
    if(names.contains("delegationState")) {
      request.setDelegationState(getQueryParameter("delegationState", query));
    }
    
    if(names.contains("candidateUser")) {
      request.setCandidateUser(getQueryParameter("candidateUser", query));
    }
    
    if(names.contains("involvedUser")) {
      request.setInvolvedUser(getQueryParameter("involvedUser", query));
    }
    
    if(names.contains("candidateGroup")) {
      request.setCandidateGroup(getQueryParameter("candidateGroup", query));
    }
    
    if(names.contains("processInstanceId")) {
      request.setProcessInstanceId(getQueryParameter("processInstanceId", query));
    }
    
    if(names.contains("processInstanceBusinessKey")) {
      request.setProcessInstanceBusinessKey(getQueryParameter("processInstanceBusinessKey", query));
    }
    
    if(names.contains("executionId")) {
      request.setExecutionId(getQueryParameter("executionId", query));
    }
    
    if(names.contains("createdOn")) {
      request.setCreatedOn(getQueryParameterAsDate("createdOn", query));
    }
    
    if(names.contains("createdBefore")) {
      request.setCreatedBefore(getQueryParameterAsDate("createdBefore", query));
    }
    
    if(names.contains("createdAfter")) {
      request.setCreatedAfter(getQueryParameterAsDate("createdAfter", query));
    }
    
    if(names.contains("excludeSubTasks")) {
      request.setExcludeSubTasks(getQueryParameterAsBoolean("excludeSubTasks", query));
    }
    
    if(names.contains("taskDefinitionKey")) {
      request.setTaskDefinitionKey(getQueryParameter("taskDefinitionKey", query));
    }
    
    if(names.contains("taskDefinitionKeyLike")) {
      request.setTaskDefinitionKeyLike(getQueryParameter("taskDefinitionKeyLike", query));
    }
    
    if(names.contains("dueDate")) {
      request.setDueDate(getQueryParameterAsDate("dueDate", query));
    }
    
    if(names.contains("dueBefore")) {
      request.setDueBefore(getQueryParameterAsDate("dueBefore", query));
    }
    
    if(names.contains("dueAfter")) {
      request.setDueAfter(getQueryParameterAsDate("dueAfter", query));
    }
    
    if(names.contains("active")) {
      request.setActive(getQueryParameterAsBoolean("active", query));
    }
    
    if(names.contains("includeTaskLocalVariables")) {
      request.setIncludeTaskLocalVariables(getQueryParameterAsBoolean("includeTaskLocalVariables", query));
    }
    
    if(names.contains("includeProcessVariables")) {
      request.setIncludeProcessVariables(getQueryParameterAsBoolean("includeProcessVariables", query));
    }
    
    return getTasksFromQueryRequest(request);
  }
}
