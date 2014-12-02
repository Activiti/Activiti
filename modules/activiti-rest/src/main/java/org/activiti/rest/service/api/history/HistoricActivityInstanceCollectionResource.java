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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Tijs Rademakers
 */
@RestController
public class HistoricActivityInstanceCollectionResource extends HistoricActivityInstanceBaseResource {

  @RequestMapping(value="/history/historic-activity-instances", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getHistoricActivityInstances(@RequestParam Map<String,String> allRequestParams, HttpServletRequest request) {
    HistoricActivityInstanceQueryRequest query = new HistoricActivityInstanceQueryRequest();

    // Populate query based on request
    if (allRequestParams.get("activityId") != null) {
      query.setActivityId(allRequestParams.get("activityId"));
    }
    
    if (allRequestParams.get("activityInstanceId") != null) {
      query.setActivityInstanceId(allRequestParams.get("activityInstanceId"));
    }
    
    if (allRequestParams.get("activityName") != null) {
      query.setActivityName(allRequestParams.get("activityName"));
    }
    
    if (allRequestParams.get("activityType") != null) {
      query.setActivityType(allRequestParams.get("activityType"));
    }
    
    if (allRequestParams.get("executionId") != null) {
      query.setExecutionId(allRequestParams.get("executionId"));
    }
    
    if (allRequestParams.get("finished") != null) {
      query.setFinished(Boolean.valueOf(allRequestParams.get("finished")));
    }
    
    if (allRequestParams.get("taskAssignee") != null) {
      query.setTaskAssignee(allRequestParams.get("taskAssignee"));
    }
    
    if (allRequestParams.get("processInstanceId") != null) {
      query.setProcessInstanceId(allRequestParams.get("processInstanceId"));
    }
    
    if (allRequestParams.get("processDefinitionId") != null) {
      query.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
    }
    
    if (allRequestParams.get("tenantId") != null) {
    	query.setTenantId(allRequestParams.get("tenantId"));
    }
    
    if (allRequestParams.get("tenantIdLike") != null) {
    	query.setTenantIdLike(allRequestParams.get("tenantIdLike"));
    }
    
    if(allRequestParams.get("withoutTenantId") != null) {
    	query.setWithoutTenantId(Boolean.valueOf(allRequestParams.get("withoutTenantId")));
    }

    return getQueryResponse(query, allRequestParams);
  }
}
