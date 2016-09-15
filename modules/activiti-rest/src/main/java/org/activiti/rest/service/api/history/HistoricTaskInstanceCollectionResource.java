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


import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.RequestUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Tijs Rademakers
 */
@RestController
public class HistoricTaskInstanceCollectionResource extends HistoricTaskInstanceBaseResource {

  @RequestMapping(value="/history/historic-task-instances", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getHistoricProcessInstances(@RequestParam Map<String,String> allRequestParams, HttpServletRequest request) {
    // Populate query based on request
    HistoricTaskInstanceQueryRequest queryRequest = new HistoricTaskInstanceQueryRequest();
    
    if (allRequestParams.get("taskId") != null) {
      queryRequest.setTaskId(allRequestParams.get("taskId"));
    }
    
    if (allRequestParams.get("processInstanceId") != null) {
      queryRequest.setProcessInstanceId(allRequestParams.get("processInstanceId"));
    }
    
    if (allRequestParams.get("processBusinessKey") != null) {
      queryRequest.setProcessBusinessKey(allRequestParams.get("processBusinessKey"));
    }

    if (allRequestParams.get("processBusinessKeyLike") != null) {
      queryRequest.setProcessBusinessKeyLike(allRequestParams.get("processBusinessKeyLike"));
    }
    
    if (allRequestParams.get("processDefinitionKey") != null) {
      queryRequest.setProcessDefinitionKey(allRequestParams.get("processDefinitionKey"));
    }
    
    if (allRequestParams.get("processDefinitionKeyLike") != null) {
      queryRequest.setProcessDefinitionKeyLike(allRequestParams.get("processDefinitionKeyLike"));
    }

    if (allRequestParams.get("processDefinitionId") != null) {
      queryRequest.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
    }
    
    if (allRequestParams.get("processDefinitionName") != null) {
      queryRequest.setProcessDefinitionName(allRequestParams.get("processDefinitionName"));
    }
    
    if (allRequestParams.get("processDefinitionNameLike") != null) {
    	queryRequest.setProcessDefinitionNameLike(allRequestParams.get("processDefinitionNameLike"));	
    }
    
    if (allRequestParams.get("executionId") != null) {
      queryRequest.setExecutionId(allRequestParams.get("executionId"));
    }
    
    if (allRequestParams.get("taskName") != null) {
      queryRequest.setTaskName(allRequestParams.get("taskName"));
    }
    
    if (allRequestParams.get("taskNameLike") != null) {
      queryRequest.setTaskNameLike(allRequestParams.get("taskNameLike"));
    }
    
    if (allRequestParams.get("taskDescription") != null) {
      queryRequest.setTaskDescription(allRequestParams.get("taskDescription"));
    }
    
    if (allRequestParams.get("taskDescriptionLike") != null) {
      queryRequest.setTaskDescriptionLike(allRequestParams.get("taskDescriptionLike"));
    }
    
    if (allRequestParams.get("taskDefinitionKey") != null) {
      queryRequest.setTaskDefinitionKey(allRequestParams.get("taskDefinitionKey"));
    }
    
    if (allRequestParams.containsKey("taskCategory")) {
      queryRequest.setTaskCategory(allRequestParams.get("taskCategory"));
    }
    
    if (allRequestParams.get("taskDeleteReason") != null) {
      queryRequest.setTaskDeleteReason(allRequestParams.get("taskDeleteReason"));
    }
    
    if (allRequestParams.get("taskDeleteReasonLike") != null) {
      queryRequest.setTaskDeleteReasonLike(allRequestParams.get("taskDeleteReasonLike"));
    }
    
    if (allRequestParams.get("taskAssignee") != null) {
      queryRequest.setTaskAssignee(allRequestParams.get("taskAssignee"));
    }
    
    if (allRequestParams.get("taskAssigneeLike") != null) {
      queryRequest.setTaskAssigneeLike(allRequestParams.get("taskAssigneeLike"));
    }
    
    if (allRequestParams.get("taskOwner") != null) {
      queryRequest.setTaskOwner(allRequestParams.get("taskOwner"));
    }
    
    if (allRequestParams.get("taskOwnerLike") != null) {
      queryRequest.setTaskOwnerLike(allRequestParams.get("taskOwnerLike"));
    }
    
    if (allRequestParams.get("taskInvolvedUser") != null) {
      queryRequest.setTaskInvolvedUser(allRequestParams.get("taskInvolvedUser"));
    }
    
    if (allRequestParams.get("taskPriority") != null) {
      queryRequest.setTaskPriority(Integer.valueOf(allRequestParams.get("taskPriority")));
    }
    
    if (allRequestParams.get("taskMinPriority") != null) {
        queryRequest.setTaskMinPriority(Integer.valueOf(allRequestParams.get("taskMinPriority")));
    }

    if (allRequestParams.get("taskMaxPriority") != null) {
        queryRequest.setTaskMaxPriority(Integer.valueOf(allRequestParams.get("taskMaxPriority")));
    }
    
    if (allRequestParams.get("finished") != null) {
      queryRequest.setFinished(Boolean.valueOf(allRequestParams.get("finished")));
    }
    
    if (allRequestParams.get("processFinished") != null) {
      queryRequest.setProcessFinished(Boolean.valueOf(allRequestParams.get("processFinished")));
    }
    
    if (allRequestParams.get("parentTaskId") != null) {
      queryRequest.setParentTaskId(allRequestParams.get("parentTaskId"));
    }
    
    if (allRequestParams.get("dueDate") != null) {
      queryRequest.setDueDate(RequestUtil.getDate(allRequestParams, "dueDate"));
    }
    
    if (allRequestParams.get("dueDateAfter") != null) {
      queryRequest.setDueDateAfter(RequestUtil.getDate(allRequestParams, "dueDateAfter"));
    }
    
    if (allRequestParams.get("dueDateBefore") != null) {
      queryRequest.setDueDateBefore(RequestUtil.getDate(allRequestParams, "dueDateBefore"));
    }
    
    if (allRequestParams.get("taskCreatedOn") != null) {
      queryRequest.setTaskCreatedOn(RequestUtil.getDate(allRequestParams, "taskCreatedOn"));
    }
    
    if (allRequestParams.get("taskCreatedBefore") != null) {
    	queryRequest.setTaskCreatedBefore(RequestUtil.getDate(allRequestParams, "taskCreatedBefore"));
    }
    
    if (allRequestParams.get("taskCreatedAfter") != null) {
    	queryRequest.setTaskCreatedAfter(RequestUtil.getDate(allRequestParams, "taskCreatedAfter"));
    }
    
    if (allRequestParams.get("taskCompletedOn") != null) {
    	queryRequest.setTaskCompletedOn(RequestUtil.getDate(allRequestParams, "taskCompletedOn"));
    }
    
    if (allRequestParams.get("taskCompletedBefore") != null) {
    	queryRequest.setTaskCompletedBefore(RequestUtil.getDate(allRequestParams, "taskCompletedBefore"));
    }
    
    if (allRequestParams.get("taskCompletedAfter") != null) {
    	queryRequest.setTaskCompletedAfter(RequestUtil.getDate(allRequestParams, "taskCompletedAfter"));
    }
    
    if (allRequestParams.get("includeTaskLocalVariables") != null) {
      queryRequest.setIncludeTaskLocalVariables(Boolean.valueOf(allRequestParams.get("includeTaskLocalVariables")));
    }
    
    if (allRequestParams.get("includeProcessVariables") != null) {
      queryRequest.setIncludeProcessVariables(Boolean.valueOf(allRequestParams.get("includeProcessVariables")));
    }
    
    if (allRequestParams.get("tenantId") != null) {
      queryRequest.setTenantId(allRequestParams.get("tenantId"));
    }
    
    if (allRequestParams.get("tenantIdLike") != null) {
    	queryRequest.setTenantIdLike(allRequestParams.get("tenantIdLike"));
    }
    
    if (allRequestParams.get("withoutTenantId") != null) {
    	queryRequest.setWithoutTenantId(Boolean.valueOf(allRequestParams.get("withoutTenantId")));
    }
    
    if (allRequestParams.get("taskCandidateGroup") != null) {
      queryRequest.setTaskCandidateGroup(allRequestParams.get("taskCandidateGroup"));
    }

    return getQueryResponse(queryRequest, allRequestParams, request.getRequestURL().toString().replace("/history/historic-task-instances", ""));
  }
}
