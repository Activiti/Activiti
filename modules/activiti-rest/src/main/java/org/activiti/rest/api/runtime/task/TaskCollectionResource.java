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

package org.activiti.rest.api.runtime.task;

import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.application.ActivitiRestServicesApplication;
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
            .createTaskReponse(this, task);
  }
  
  @Get
  public DataResponse getTasks() {
    if(!authenticate()) { return null; }
    
    // Create a Task query request
    TaskQueryRequest request = new TaskQueryRequest();
    Form query = getQuery();
    
    // Populate filter-parameters
    if(query.getNames().contains("name")) {
      request.setName(getQueryParameter("name", query));
    }
    
    if(query.getNames().contains("nameLike")) {
      request.setNameLike(getQueryParameter("nameLike", query));
    }
    
    if(query.getNames().contains("description")) {
      request.setDescription(getQueryParameter("description", query));
    }
    
    if(query.getNames().contains("descriptionLike")) {
      request.setDescriptionLike(getQueryParameter("descriptionLike", query));
    }
    
    if(query.getNames().contains("priority")) {
      request.setPriority(getQueryParameterAsInt("priority", query));
    }
    
    if(query.getNames().contains("minimumPriority")) {
      request.setMinimumPriority(getQueryParameterAsInt("minimumPriority", query));
    }
    
    if(query.getNames().contains("maximumPriority")) {
      request.setMaximumPriority(getQueryParameterAsInt("maximumPriority", query));
    }
    
    if(query.getNames().contains("assignee")) {
      request.setAssignee(getQueryParameter("assignee", query));
    }
    
    if(query.getNames().contains("owner")) {
      request.setOwner(getQueryParameter("owner", query));
    }
    
    if(query.getNames().contains("unassigned")) {
      request.setUnassigned(getQueryParameterAsBoolean("unassigned", query));
    }
    
    if(query.getNames().contains("delegationState")) {
      request.setDelegationState(getQueryParameter("delegationState", query));
    }
    
    if(query.getNames().contains("candidateUser")) {
      request.setCandidateUser(getQueryParameter("candidateUser", query));
    }
    
    if(query.getNames().contains("involvedUser")) {
      request.setInvolvedUser(getQueryParameter("involvedUser", query));
    }
    
    if(query.getNames().contains("candidateGroup")) {
      request.setCandidateGroup(getQueryParameter("candidateGroup", query));
    }
    
    if(query.getNames().contains("processInstanceId")) {
      request.setProcessInstanceId(getQueryParameter("processInstanceId", query));
    }
    
    if(query.getNames().contains("processInstanceBusinessKey")) {
      request.setProcessInstanceBusinessKey(getQueryParameter("processInstanceBusinessKey", query));
    }
    
    if(query.getNames().contains("executionId")) {
      request.setExecutionId(getQueryParameter("executionId", query));
    }
    
    if(query.getNames().contains("createdOn")) {
      request.setCreatedOn(getQueryParameterAsDate("createdOn", query));
    }
    
    if(query.getNames().contains("createdBefore")) {
      request.setCreatedBefore(getQueryParameterAsDate("createdBefore", query));
    }
    
    if(query.getNames().contains("createdAfter")) {
      request.setCreatedAfter(getQueryParameterAsDate("createdAfter", query));
    }
    
    if(query.getNames().contains("excludeSubTasks")) {
      request.setExcludeSubTasks(getQueryParameterAsBoolean("excludeSubTasks", query));
    }
    
    if(query.getNames().contains("taskDefinitionKey")) {
      request.setTaskDefinitionKey(getQueryParameter("taskDefinitionKey", query));
    }
    
    if(query.getNames().contains("taskDefinitionKeyLike")) {
      request.setTaskDefinitionKeyLike(getQueryParameter("taskDefinitionKeyLike", query));
    }
    
    if(query.getNames().contains("dueDate")) {
      request.setDueDate(getQueryParameterAsDate("dueDate", query));
    }
    
    if(query.getNames().contains("dueBefore")) {
      request.setDueBefore(getQueryParameterAsDate("dueBefore", query));
    }
    
    if(query.getNames().contains("dueAfter")) {
      request.setDueAfter(getQueryParameterAsDate("dueAfter", query));
    }
    
    if(query.getNames().contains("active")) {
      request.setActive(getQueryParameterAsBoolean("active", query));
    }
    
    return getTasksFromQueryRequest(request);
  }
}
