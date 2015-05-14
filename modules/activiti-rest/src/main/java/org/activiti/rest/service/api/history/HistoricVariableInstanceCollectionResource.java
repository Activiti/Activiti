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
public class HistoricVariableInstanceCollectionResource extends HistoricVariableInstanceBaseResource {

  @RequestMapping(value="/history/historic-variable-instances", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getHistoricActivityInstances(@RequestParam Map<String,String> allRequestParams, HttpServletRequest request) {
    HistoricVariableInstanceQueryRequest query = new HistoricVariableInstanceQueryRequest();

    // Populate query based on request
    if (allRequestParams.get("excludeTaskVariables") != null) {
      query.setExcludeTaskVariables(Boolean.valueOf(allRequestParams.get("excludeTaskVariables")));
    }
    
    if (allRequestParams.get("taskId") != null) {
      query.setTaskId(allRequestParams.get("taskId"));
    }
    
    if(allRequestParams.get("executionId") != null)
    {
      query.setExecutionId(allRequestParams.get("executionId"));
    }

    if (allRequestParams.get("processInstanceId") != null) {
      query.setProcessInstanceId(allRequestParams.get("processInstanceId"));
    }
    
    if (allRequestParams.get("variableName") != null) {
      query.setVariableName(allRequestParams.get("variableName"));
    }
    
    if (allRequestParams.get("variableNameLike") != null) {
      query.setVariableNameLike(allRequestParams.get("variableNameLike"));
    }
    
    return getQueryResponse(query, allRequestParams);
  }
}
