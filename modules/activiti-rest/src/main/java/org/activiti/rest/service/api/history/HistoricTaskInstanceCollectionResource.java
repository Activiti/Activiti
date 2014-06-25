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

package org.activiti.rest.service.api.history;


import org.activiti.rest.common.api.DataResponse;
import org.restlet.data.Form;
import org.restlet.resource.Get;


/**
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceCollectionResource extends HistoricTaskInstanceBaseResource {

  @Get("json")
  public DataResponse getHistoricProcessInstances() {
    if(!authenticate()) {
      return null;
    }
    Form urlQuery = getQuery();
   
    // Populate query based on request
    HistoricTaskInstanceQueryRequest queryRequest = new HistoricTaskInstanceQueryRequest();
    
    if(getQueryParameter("taskId", urlQuery) != null) {
      queryRequest.setTaskId(getQueryParameter("taskId", urlQuery));
    }
    
    if(getQueryParameter("processInstanceId", urlQuery) != null) {
      queryRequest.setProcessInstanceId(getQueryParameter("processInstanceId", urlQuery));
    }
    
    if(getQueryParameter("processBusinessKey", urlQuery) != null) {
      queryRequest.setProcessBusinessKey(getQueryParameter("processBusinessKey", urlQuery));
    }
    
    if(getQueryParameter("processDefinitionKey", urlQuery) != null) {
      queryRequest.setProcessDefinitionKey(getQueryParameter("processDefinitionKey", urlQuery));
    }
    
    if(getQueryParameter("processDefinitionId", urlQuery) != null) {
      queryRequest.setProcessDefinitionId(getQueryParameter("processDefinitionId", urlQuery));
    }
    
    if(getQueryParameter("processDefinitionName", urlQuery) != null) {
      queryRequest.setProcessDefinitionName(getQueryParameter("processDefinitionName", urlQuery));
    }
    
    if(getQueryParameter("executionId", urlQuery) != null) {
      queryRequest.setExecutionId(getQueryParameter("executionId", urlQuery));
    }
    
    if(getQueryParameter("taskName", urlQuery) != null) {
      queryRequest.setTaskName(getQueryParameter("taskName", urlQuery));
    }
    
    if(getQueryParameter("taskNameLike", urlQuery) != null) {
      queryRequest.setTaskNameLike(getQueryParameter("taskNameLike", urlQuery));
    }
    
    if(getQueryParameter("taskDescription", urlQuery) != null) {
      queryRequest.setTaskDescription(getQueryParameter("taskDescription", urlQuery));
    }
    
    if(getQueryParameter("taskDescriptionLike", urlQuery) != null) {
      queryRequest.setTaskDescriptionLike(getQueryParameter("taskDescriptionLike", urlQuery));
    }
    
    if(getQueryParameter("taskDefinitionKey", urlQuery) != null) {
      queryRequest.setTaskDefinitionKey(getQueryParameter("taskDefinitionKey", urlQuery));
    }
    
    if(getQueryParameter("taskDeleteReason", urlQuery) != null) {
      queryRequest.setTaskDeleteReason(getQueryParameter("taskDeleteReason", urlQuery));
    }
    
    if(getQueryParameter("taskDeleteReasonLike", urlQuery) != null) {
      queryRequest.setTaskDeleteReasonLike(getQueryParameter("taskDeleteReasonLike", urlQuery));
    }
    
    if(getQueryParameter("taskAssignee", urlQuery) != null) {
      queryRequest.setTaskAssignee(getQueryParameter("taskAssignee", urlQuery));
    }
    
    if(getQueryParameter("taskAssigneeLike", urlQuery) != null) {
      queryRequest.setTaskAssigneeLike(getQueryParameter("taskAssigneeLike", urlQuery));
    }
    
    if(getQueryParameter("taskOwner", urlQuery) != null) {
      queryRequest.setTaskOwner(getQueryParameter("taskOwner", urlQuery));
    }
    
    if(getQueryParameter("taskOwnerLike", urlQuery) != null) {
      queryRequest.setTaskOwnerLike(getQueryParameter("taskOwnerLike", urlQuery));
    }
    
    if(getQueryParameter("taskInvolvedUser", urlQuery) != null) {
      queryRequest.setTaskInvolvedUser(getQueryParameter("taskInvolvedUser", urlQuery));
    }
    
    if(getQueryParameter("taskPriority", urlQuery) != null) {
      queryRequest.setTaskPriority(getQueryParameterAsInt("taskPriority", urlQuery));
    }
    
    if(getQueryParameter("finished", urlQuery) != null) {
      queryRequest.setFinished(getQueryParameterAsBoolean("finished", urlQuery));
    }
    
    if(getQueryParameter("processFinished", urlQuery) != null) {
      queryRequest.setProcessFinished(getQueryParameterAsBoolean("processFinished", urlQuery));
    }
    
    if(getQueryParameter("parentTaskId", urlQuery) != null) {
      queryRequest.setParentTaskId(getQueryParameter("parentTaskId", urlQuery));
    }
    
    if(getQueryParameter("dueDate", urlQuery) != null) {
      queryRequest.setDueDate(getQueryParameterAsDate("dueDate", urlQuery));
    }
    
    if(getQueryParameter("dueDateAfter", urlQuery) != null) {
      queryRequest.setDueDateAfter(getQueryParameterAsDate("dueDateAfter", urlQuery));
    }
    
    if(getQueryParameter("dueDateBefore", urlQuery) != null) {
      queryRequest.setDueDateBefore(getQueryParameterAsDate("dueDateBefore", urlQuery));
    }
    
    if(getQueryParameter("taskCreatedOn", urlQuery) != null) {
      queryRequest.setTaskCreatedOn(getQueryParameterAsDate("taskCreatedOn", urlQuery));
    }
    
    if(getQueryParameter("taskCreatedBefore", urlQuery) != null) {
    	queryRequest.setTaskCreatedBefore(getQueryParameterAsDate("taskCreatedBefore", urlQuery));
    }
    
    if(getQueryParameter("taskCreatedAfter", urlQuery) != null) {
    	queryRequest.setTaskCreatedAfter(getQueryParameterAsDate("taskCreatedAfter", urlQuery));
    }
    
    if(getQueryParameter("taskCompletedOn", urlQuery) != null) {
    	queryRequest.setTaskCompletedOn(getQueryParameterAsDate("taskCompletedOn", urlQuery));
    }
    
    if(getQueryParameter("taskCompletedBefore", urlQuery) != null) {
    	queryRequest.setTaskCompletedBefore(getQueryParameterAsDate("taskCompletedBefore", urlQuery));
    }
    
    if(getQueryParameter("taskCompletedAfter", urlQuery) != null) {
    	queryRequest.setTaskCompletedAfter(getQueryParameterAsDate("taskCompletedAfter", urlQuery));
    }
    
    if(getQueryParameter("includeTaskLocalVariables", urlQuery) != null) {
      queryRequest.setIncludeTaskLocalVariables(getQueryParameterAsBoolean("includeTaskLocalVariables", urlQuery));
    }
    
    if(getQueryParameter("includeProcessVariables", urlQuery) != null) {
      queryRequest.setIncludeProcessVariables(getQueryParameterAsBoolean("includeProcessVariables", urlQuery));
    }
    
    if(getQueryParameter("tenantId", urlQuery) != null) {
      queryRequest.setTenantId(getQueryParameter("tenantId", urlQuery));
    }
    
    if(getQueryParameter("tenantIdLike", urlQuery) != null) {
    	queryRequest.setTenantIdLike(getQueryParameter("tenantIdLike", urlQuery));
    }
    
    if(getQueryParameter("withoutTenantId", urlQuery) != null) {
    	queryRequest.setWithoutTenantId(getQueryParameterAsBoolean("withoutTenantId", urlQuery));
    }
    
    return getQueryResponse(queryRequest, urlQuery);
  }
}
