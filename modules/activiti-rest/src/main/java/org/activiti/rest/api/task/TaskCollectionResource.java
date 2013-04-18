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

import org.activiti.engine.impl.TaskQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
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
public class TaskCollectionResource extends TaskBasedResource {

  private static HashMap<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  static {
    properties.put("id", TaskQueryProperty.TASK_ID);
    properties.put("name", TaskQueryProperty.NAME);
    properties.put("description", TaskQueryProperty.DESCRIPTION);
    properties.put("dueDate", TaskQueryProperty.DUE_DATE);
    properties.put("createTime", TaskQueryProperty.CREATE_TIME);
    properties.put("priority", TaskQueryProperty.PRIORITY);
    properties.put("executionId", TaskQueryProperty.EXECUTION_ID);
    properties.put("processInstanceId", TaskQueryProperty.PROCESS_INSTANCE_ID);
  }
  
  
  @Post
  public TaskResponse createTask(TaskRequest taskRequest) {
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
    TaskQuery taskQuery = ActivitiUtil.getTaskService().createTaskQuery();
    Form query = getQuery();
    
    // Populate filter-parameters
    if(query.getNames().contains("name")) {
      taskQuery.taskName(getQueryParameter("name", query));
    }
    
    if(query.getNames().contains("nameLike")) {
      taskQuery.taskNameLike(getQueryParameter("nameLike", query));
    }
    
    if(query.getNames().contains("description")) {
      taskQuery.taskDescription(getQueryParameter("description", query));
    }
    
    if(query.getNames().contains("descriptionLike")) {
      taskQuery.taskDescriptionLike(getQueryParameter("descriptionLike", query));
    }
    
    if(query.getNames().contains("priority")) {
      taskQuery.taskPriority(getQueryParameterAsInt("priority", query));
    }
    
    if(query.getNames().contains("minimumPriority")) {
      taskQuery.taskMinPriority(getQueryParameterAsInt("minimumPriority", query));
    }
    
    if(query.getNames().contains("maximumPriority")) {
      taskQuery.taskMaxPriority(getQueryParameterAsInt("maximumPriority", query));
    }
    
    if(query.getNames().contains("assignee")) {
      taskQuery.taskAssignee(getQueryParameter("assignee", query));
    }
    
    if(query.getNames().contains("owner")) {
      taskQuery.taskOwner(getQueryParameter("owner", query));
    }
    
    if(query.getNames().contains("unassigned")) {
      Boolean unassigned = getQueryParameterAsBoolean("unassigned", query);
      if(unassigned != null && unassigned) {
        taskQuery.taskUnassigned();
      }
    }
    
    if(query.getNames().contains("delegationState")) {
      String delegationStateString = getQueryParameter("delegationState", query);
      DelegationState state = getDelegationState(delegationStateString);
      if(state != null) {
        taskQuery.taskDelegationState(state);
      }
    }
    
    if(query.getNames().contains("candidateUser")) {
      taskQuery.taskCandidateUser(getQueryParameter("candidateUser", query));
    }
    
    if(query.getNames().contains("involvedUser")) {
      taskQuery.taskInvolvedUser(getQueryParameter("involvedUser", query));
    }
    
    if(query.getNames().contains("candidateGroup")) {
      taskQuery.taskCandidateGroup(getQueryParameter("candidateGroup", query));
    }
    
    if(query.getNames().contains("processInstanceId")) {
      taskQuery.processInstanceId(getQueryParameter("processInstanceId", query));
    }
    
    if(query.getNames().contains("processInstanceBusinessKey")) {
      taskQuery.processInstanceBusinessKey(getQueryParameter("processInstanceBusinessKey", query));
    }
    
    if(query.getNames().contains("executionId")) {
      taskQuery.executionId(getQueryParameter("executionId", query));
    }
    
    if(query.getNames().contains("createdOn")) {
      taskQuery.taskCreatedOn(getQueryParameterAsDate("createdOn", query));
    }
    
    if(query.getNames().contains("createdBefore")) {
      taskQuery.taskCreatedBefore(getQueryParameterAsDate("createdBefore", query));
    }
    
    if(query.getNames().contains("createdAfter")) {
      taskQuery.taskCreatedAfter(getQueryParameterAsDate("createdAfter", query));
    }
    
    if(query.getNames().contains("excludeSubTasks")) {
      Boolean excludeSubTasks = getQueryParameterAsBoolean("excludeSubTasks", query);
      if(excludeSubTasks != null && excludeSubTasks) {
        taskQuery.excludeSubtasks();
      }
    }
    
    if(query.getNames().contains("taskDefinitionKey")) {
      taskQuery.taskDefinitionKey(getQueryParameter("taskDefinitionKey", query));
    }
    
    if(query.getNames().contains("taskDefinitionKeyLike")) {
      taskQuery.taskDefinitionKeyLike(getQueryParameter("taskDefinitionKeyLike", query));
    }
    
    if(query.getNames().contains("dueDate")) {
      taskQuery.dueDate(getQueryParameterAsDate("dueDate", query));
    }
    
    if(query.getNames().contains("dueBefore")) {
      taskQuery.dueBefore(getQueryParameterAsDate("dueBefore", query));
    }
    
    if(query.getNames().contains("dueAfter")) {
      taskQuery.dueAfter(getQueryParameterAsDate("dueAfter", query));
    }
    
    if(query.getNames().contains("active")) {
      Boolean active = getQueryParameterAsBoolean("active", query);
      if(active != null) {
        if(active) {
          taskQuery.active();
        } else {
          taskQuery.suspended();
        }
      }
    }
    
    // TODO: populate query based on variable-values  
    return new TaskPaginateList(this).paginateList(query, taskQuery, "id", properties);
  }
}
