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

package org.activiti.rest.service.api.legacy.process;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.ProcessDefinitionQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.SecuredResource;
import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
@Deprecated
public class ProcessDefinitionsResource extends SecuredResource {
  
  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  public ProcessDefinitionsResource() {
    properties.put("id", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
    properties.put("key", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_KEY);
    properties.put("version", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_VERSION);
    properties.put("deploymentId", ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
    properties.put("name", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME);
    properties.put("category", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_CATEGORY);
  }
  
  @Get
  public DataResponse getProcessDefinitions() {
    if(authenticate() == false) return null;

    ProcessDefinitionQuery query = ActivitiUtil.getRepositoryService().createProcessDefinitionQuery();
    String startableByUser = getQuery().getValues("startableByUser");
    if (StringUtils.isNotEmpty(startableByUser)) {
      query = query.startableByUser(startableByUser);
    }
    
    DataResponse response = new LegacyProcessDefinitionsPaginateList().paginateList(
        getQuery(), query, "id", properties);
    return response;
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
}
