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

package org.activiti.rest.api.process;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.ProcessInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.data.Form;

/**
 * @author Frederik Heremans
 */
public class ProcessInstanceBasedResource extends SecuredResource {

  private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();

  static {
    allowedSortProperties.put("processDefinitionId", ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
    allowedSortProperties.put("processDefinitionKey", ProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY);
    allowedSortProperties.put("id", ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID);
  }
  
  protected DataResponse getQueryResponse(ProcessInstanceQueryRequest queryRequest, Form urlQuery) {
    ProcessInstanceQuery query = ActivitiUtil.getRuntimeService().createProcessInstanceQuery();
    
    // Populate query based on request
    if(queryRequest.getProcessInstanceId() != null) {
      query.processInstanceId(queryRequest.getProcessInstanceId());
    }
    if(queryRequest.getProcessDefinitionKey() != null) {
      query.processDefinitionKey(queryRequest.getProcessDefinitionKey());
    }
    if(queryRequest.getProcessDefinitionId() != null) {
      query.processDefinitionId(queryRequest.getProcessDefinitionId());
    }
    if(queryRequest.getProcessBusinessKey() != null) {
      query.processInstanceBusinessKey(queryRequest.getProcessBusinessKey());
    }
    if(queryRequest.getInvolvedUser() != null) {
      query.involvedUser(queryRequest.getInvolvedUser());
    }
    if(queryRequest.getSuspended() != null) {
      if(queryRequest.getSuspended()) {
        query.suspended();
      } else {
        query.active();
      }
    }
    if(queryRequest.getSubProcessInstanceId() != null) {
      query.subProcessInstanceId(queryRequest.getSubProcessInstanceId());
    }
    if(queryRequest.getSuperProcessInstanceId() != null) {
      query.superProcessInstanceId(queryRequest.getSuperProcessInstanceId());
    }

    return new ProcessInstancePaginateList(this).paginateList(urlQuery, query, "id", allowedSortProperties);
  }
}
