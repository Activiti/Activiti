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

package org.activiti.rest.api.repository;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.ProcessDefinitionQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.data.Form;
import org.restlet.resource.Get;

/**
 * @author Frederik Heremans
 */
public class ProcessDefinitionCollectionResource extends SecuredResource {
  
  private static final Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  static {
    properties.put("id", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
    properties.put("key", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_KEY);
    properties.put("category", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_CATEGORY);
    properties.put("name", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME);
    properties.put("version", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_VERSION);
    properties.put("deploymentId", ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
  }
  
  @Get
  public DataResponse getProcessDefinitions() {
    if(authenticate() == false) return null;

    ProcessDefinitionQuery processDefinitionQuery = ActivitiUtil.getRepositoryService().createProcessDefinitionQuery();
    Form query = getQuery();
    
    // Populate filter-parameters
    if(query.getNames().contains("category")) {
      processDefinitionQuery.processDefinitionCategory(getQueryParameter("category", query));
    }
    if(query.getNames().contains("categoryLike")) {
      processDefinitionQuery.processDefinitionCategoryLike(getQueryParameter("categoryLike", query));
    }
    if(query.getNames().contains("categoryNotEquals")) {
      processDefinitionQuery.processDefinitionCategoryNotEquals(getQueryParameter("categoryNotEquals", query));
    }
    if(query.getNames().contains("key")) {
      processDefinitionQuery.processDefinitionKey(getQueryParameter("key", query));
    }
    if(query.getNames().contains("keyLike")) {
      processDefinitionQuery.processDefinitionKeyLike(getQueryParameter("keyLike", query));
    }
    if(query.getNames().contains("name")) {
      processDefinitionQuery.processDefinitionName(getQueryParameter("name", query));
    }
    if(query.getNames().contains("nameLike")) {
      processDefinitionQuery.processDefinitionNameLike(getQueryParameter("nameLike", query));
    }
    if(query.getNames().contains("resourceName")) {
      processDefinitionQuery.processDefinitionResourceName(getQueryParameter("resourceName", query));
    }
    if(query.getNames().contains("resourceNameLike")) {
      processDefinitionQuery.processDefinitionResourceNameLike(getQueryParameter("resourceNameLike", query));
    }
    if(query.getNames().contains("version")) {
      processDefinitionQuery.processDefinitionVersion(getQueryParameterAsInt("version", query));
    }
    if(query.getNames().contains("suspended")) {
      Boolean suspended = getQueryParameterAsBoolean("suspended", query);
      if(suspended != null) {
        if(suspended) {
          processDefinitionQuery.suspended();
        } else {
          processDefinitionQuery.active();
        }
      }
    }
    if(query.getNames().contains("latest")) {
      Boolean latest = getQueryParameterAsBoolean("latest", query);
      if(latest != null && latest) {
        processDefinitionQuery.latestVersion();
      }
    }
    if(query.getNames().contains("deploymentId")) {
      processDefinitionQuery.deploymentId(getQueryParameter("deploymentId", query));
    }
    if(query.getNames().contains("startableByUser")) {
      processDefinitionQuery.startableByUser(getQueryParameter("startableByUser", query));
    }
    
    return new ProcessDefinitionsPaginateList(this).paginateList(getQuery(), processDefinitionQuery, "name", properties);
  }
}
