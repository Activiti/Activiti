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
public class HistoricProcessInstanceCollectionResource extends HistoricProcessInstanceBaseResource {

  @Get
  public DataResponse getHistoricProcessInstances() {
    if(!authenticate()) {
      return null;
    }
    Form urlQuery = getQuery();
   
    // Populate query based on request
    HistoricProcessInstanceQueryRequest queryRequest = new HistoricProcessInstanceQueryRequest();
    
    if(getQueryParameter("processInstanceId", urlQuery) != null) {
      queryRequest.setProcessInstanceId(getQueryParameter("processInstanceId", urlQuery));
    }
    
    if(getQueryParameter("processDefinitionKey", urlQuery) != null) {
      queryRequest.setProcessDefinitionKey(getQueryParameter("processDefinitionKey", urlQuery));
    }
    
    if(getQueryParameter("processDefinitionId", urlQuery) != null) {
      queryRequest.setProcessDefinitionId(getQueryParameter("processDefinitionId", urlQuery));
    }
    
    if(getQueryParameter("businessKey", urlQuery) != null) {
      queryRequest.setProcessBusinessKey(getQueryParameter("businessKey", urlQuery));
    }
    
    if(getQueryParameter("involvedUser", urlQuery) != null) {
      queryRequest.setInvolvedUser(getQueryParameter("involvedUser", urlQuery));
    }
    
    if(getQueryParameter("finished", urlQuery) != null) {
      queryRequest.setFinished(getQueryParameterAsBoolean("finished", urlQuery));
    }
    
    if(getQueryParameter("superProcessInstanceId", urlQuery) != null) {
      queryRequest.setSuperProcessInstanceId(getQueryParameter("superProcessInstanceId", urlQuery));
    }
    
    if(getQueryParameter("excludeSubprocesses", urlQuery) != null) {
      queryRequest.setExcludeSubprocesses(getQueryParameterAsBoolean("excludeSubprocesses", urlQuery));
    }
    
    if(getQueryParameter("finishedAfter", urlQuery) != null) {
      queryRequest.setFinishedAfter(getQueryParameterAsDate("finishedAfter", urlQuery));
    }
    
    if(getQueryParameter("finishedBefore", urlQuery) != null) {
      queryRequest.setFinishedBefore(getQueryParameterAsDate("finishedBefore", urlQuery));
    }
    
    if(getQueryParameter("startedAfter", urlQuery) != null) {
      queryRequest.setStartedAfter(getQueryParameterAsDate("startedAfter", urlQuery));
    }
    
    if(getQueryParameter("startedBefore", urlQuery) != null) {
      queryRequest.setStartedBefore(getQueryParameterAsDate("startedBefore", urlQuery));
    }
    
    if(getQueryParameter("startedBy", urlQuery) != null) {
      queryRequest.setStartedBy(getQueryParameter("startedBy", urlQuery));
    }
    
    if(getQueryParameter("includeProcessVariables", urlQuery) != null) {
      queryRequest.setIncludeProcessVariables(getQueryParameterAsBoolean("includeProcessVariables", urlQuery));
    }
    
    return getQueryResponse(queryRequest, urlQuery);
  }
}
