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
public class HistoricVariableInstanceCollectionResource extends HistoricVariableInstanceBaseResource {

  @Get
  public DataResponse getHistoricActivityInstances() {
    if(!authenticate()) {
      return null;
    }
    Form urlQuery = getQuery();
    
    HistoricVariableInstanceQueryRequest query = new HistoricVariableInstanceQueryRequest();

    // Populate query based on request
    if (getQueryParameter("excludeTaskVariables", urlQuery) != null) {
      Boolean excludeTaskVariables = getQueryParameterAsBoolean("excludeTaskVariables", urlQuery);
      query.setExcludeTaskVariables(excludeTaskVariables);
    }
    
    if (getQueryParameter("taskId", urlQuery) != null) {
      query.setTaskId(getQueryParameter("taskId", urlQuery));
    }
    
    if (getQueryParameter("processInstanceId", urlQuery) != null) {
      query.setProcessInstanceId(getQueryParameter("processInstanceId", urlQuery));
    }
    
    if (getQueryParameter("variableName", urlQuery) != null) {
      query.setVariableName(getQueryParameter("variableName", urlQuery));
    }
    
    if (getQueryParameter("variableNameLike", urlQuery) != null) {
      query.setVariableNameLike(getQueryParameter("variableNameLike", urlQuery));
    }
    
    return getQueryResponse(query, urlQuery);
  }
}
