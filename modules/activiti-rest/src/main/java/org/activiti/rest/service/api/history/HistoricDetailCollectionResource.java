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
public class HistoricDetailCollectionResource extends HistoricDetailBaseResource {

  @RequestMapping(value="/history/historic-detail", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getHistoricDetailInfo(@RequestParam Map<String,String> allRequestParams, HttpServletRequest request) {
    // Populate query based on request
    HistoricDetailQueryRequest queryRequest = new HistoricDetailQueryRequest();
    
    if (allRequestParams.get("id") != null) {
      queryRequest.setId(allRequestParams.get("id"));
    }
    
    if (allRequestParams.get("processInstanceId") != null) {
      queryRequest.setProcessInstanceId(allRequestParams.get("processInstanceId"));
    }
    
    if (allRequestParams.get("executionId") != null) {
      queryRequest.setExecutionId(allRequestParams.get("executionId"));
    }
    
    if (allRequestParams.get("activityInstanceId") != null) {
      queryRequest.setActivityInstanceId(allRequestParams.get("activityInstanceId"));
    }
    
    if (allRequestParams.get("taskId") != null) {
      queryRequest.setTaskId(allRequestParams.get("taskId"));
    }
    
    if (allRequestParams.get("selectOnlyFormProperties") != null) {
      queryRequest.setSelectOnlyFormProperties(Boolean.valueOf(allRequestParams.get("selectOnlyFormProperties")));
    }
    
    if (allRequestParams.get("selectOnlyVariableUpdates") != null) {
      queryRequest.setSelectOnlyVariableUpdates(Boolean.valueOf(allRequestParams.get("selectOnlyVariableUpdates")));
    }
    
    return getQueryResponse(queryRequest, allRequestParams);
  }
}
