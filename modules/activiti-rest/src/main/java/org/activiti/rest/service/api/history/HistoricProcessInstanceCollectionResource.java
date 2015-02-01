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
public class HistoricProcessInstanceCollectionResource extends HistoricProcessInstanceBaseResource {

  @RequestMapping(value="/history/historic-process-instances", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getHistoricProcessInstances(@RequestParam Map<String,String> allRequestParams, HttpServletRequest request) {
    // Populate query based on request
    HistoricProcessInstanceQueryRequest queryRequest = new HistoricProcessInstanceQueryRequest();
    
    if (allRequestParams.get("processInstanceId") != null) {
      queryRequest.setProcessInstanceId(allRequestParams.get("processInstanceId"));
    }
    
    if (allRequestParams.get("processDefinitionKey") != null) {
      queryRequest.setProcessDefinitionKey(allRequestParams.get("processDefinitionKey"));
    }
    
    if (allRequestParams.get("processDefinitionId") != null) {
      queryRequest.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
    }
    
    if (allRequestParams.get("businessKey") != null) {
      queryRequest.setProcessBusinessKey(allRequestParams.get("businessKey"));
    }
    
    if (allRequestParams.get("involvedUser") != null) {
      queryRequest.setInvolvedUser(allRequestParams.get("involvedUser"));
    }
    
    if (allRequestParams.get("finished") != null) {
      queryRequest.setFinished(Boolean.valueOf(allRequestParams.get("finished")));
    }
    
    if (allRequestParams.get("superProcessInstanceId") != null) {
      queryRequest.setSuperProcessInstanceId(allRequestParams.get("superProcessInstanceId"));
    }
    
    if (allRequestParams.get("excludeSubprocesses") != null) {
      queryRequest.setExcludeSubprocesses(Boolean.valueOf(allRequestParams.get("excludeSubprocesses")));
    }
    
    if (allRequestParams.get("finishedAfter") != null) {
      queryRequest.setFinishedAfter(RequestUtil.getDate(allRequestParams, "finishedAfter"));
    }
    
    if (allRequestParams.get("finishedBefore") != null) {
      queryRequest.setFinishedBefore(RequestUtil.getDate(allRequestParams, "finishedBefore"));
    }
    
    if (allRequestParams.get("startedAfter") != null) {
      queryRequest.setStartedAfter(RequestUtil.getDate(allRequestParams, "startedAfter"));
    }
    
    if (allRequestParams.get("startedBefore") != null) {
      queryRequest.setStartedBefore(RequestUtil.getDate(allRequestParams, "startedBefore"));
    }
    
    if (allRequestParams.get("startedBy") != null) {
      queryRequest.setStartedBy(allRequestParams.get("startedBy"));
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
    
    return getQueryResponse(queryRequest, allRequestParams);
  }
}
