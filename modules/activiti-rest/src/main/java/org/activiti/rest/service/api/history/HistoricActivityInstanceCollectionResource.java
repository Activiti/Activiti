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
public class HistoricActivityInstanceCollectionResource extends HistoricActivityInstanceBaseResource {

  @Get("json")
  public DataResponse getHistoricActivityInstances() {
    if(!authenticate()) {
      return null;
    }
    Form urlQuery = getQuery();
    
    HistoricActivityInstanceQueryRequest query = new HistoricActivityInstanceQueryRequest();

    // Populate query based on request
    if (getQueryParameter("activityId", urlQuery) != null) {
      query.setActivityId(getQueryParameter("activityId", urlQuery));
    }
    
    if (getQueryParameter("activityInstanceId", urlQuery) != null) {
      query.setActivityInstanceId(getQueryParameter("activityInstanceId", urlQuery));
    }
    
    if (getQueryParameter("activityName", urlQuery) != null) {
      query.setActivityName(getQueryParameter("activityName", urlQuery));
    }
    
    if (getQueryParameter("activityType", urlQuery) != null) {
      query.setActivityType(getQueryParameter("activityType", urlQuery));
    }
    
    if (getQueryParameter("executionId", urlQuery) != null) {
      query.setExecutionId(getQueryParameter("executionId", urlQuery));
    }
    
    if (getQueryParameter("finished", urlQuery) != null) {
      query.setFinished(getQueryParameterAsBoolean("finished", urlQuery));
    }
    
    if (getQueryParameter("taskAssignee", urlQuery) != null) {
      query.setTaskAssignee(getQueryParameter("taskAssignee", urlQuery));
    }
    
    if (getQueryParameter("processInstanceId", urlQuery) != null) {
      query.setProcessInstanceId(getQueryParameter("processInstanceId", urlQuery));
    }
    
    if (getQueryParameter("processDefinitionId", urlQuery) != null) {
      query.setProcessDefinitionId(getQueryParameter("processDefinitionId", urlQuery));
    }
    
    if(getQueryParameter("tenantId", urlQuery) != null) {
    	query.setTenantId(getQueryParameter("tenantId", urlQuery));
    }
    
    if(getQueryParameter("tenantIdLike", urlQuery) != null) {
    	query.setTenantIdLike(getQueryParameter("tenantIdLike", urlQuery));
    }
    
    if(getQueryParameter("withoutTenantId", urlQuery) != null) {
    	query.setWithoutTenantId(getQueryParameterAsBoolean("withoutTenantId", urlQuery));
    }

    return getQueryResponse(query, urlQuery);
  }
}
