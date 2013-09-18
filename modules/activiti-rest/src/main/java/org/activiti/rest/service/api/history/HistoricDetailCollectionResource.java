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
public class HistoricDetailCollectionResource extends HistoricDetailBaseResource {

  @Get
  public DataResponse getHistoricDetailInfo() {
    if (!authenticate()) {
      return null;
    }
    Form urlQuery = getQuery();
   
    // Populate query based on request
    HistoricDetailQueryRequest queryRequest = new HistoricDetailQueryRequest();
    
    if (getQueryParameter("id", urlQuery) != null) {
      queryRequest.setId(getQueryParameter("id", urlQuery));
    }
    
    if (getQueryParameter("processInstanceId", urlQuery) != null) {
      queryRequest.setProcessInstanceId(getQueryParameter("processInstanceId", urlQuery));
    }
    
    if (getQueryParameter("executionId", urlQuery) != null) {
      queryRequest.setExecutionId(getQueryParameter("executionId", urlQuery));
    }
    
    if (getQueryParameter("activityInstanceId", urlQuery) != null) {
      queryRequest.setActivityInstanceId(getQueryParameter("activityInstanceId", urlQuery));
    }
    
    if (getQueryParameter("taskId", urlQuery) != null) {
      queryRequest.setTaskId(getQueryParameter("taskId", urlQuery));
    }
    
    if (getQueryParameter("selectOnlyFormProperties", urlQuery) != null) {
      queryRequest.setSelectOnlyFormProperties(getQueryParameterAsBoolean("selectOnlyFormProperties", urlQuery));
    }
    
    if (getQueryParameter("selectOnlyVariableUpdates", urlQuery) != null) {
      queryRequest.setSelectOnlyVariableUpdates(getQueryParameterAsBoolean("selectOnlyVariableUpdates", urlQuery));
    }
    
    return getQueryResponse(queryRequest, urlQuery);
  }
}
