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

package org.activiti.rest.service.api.repository;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.ProcessDefinitionQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class ProcessDefinitionCollectionResource {
  
  private static final Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  static {
    properties.put("id", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
    properties.put("key", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_KEY);
    properties.put("category", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_CATEGORY);
    properties.put("name", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME);
    properties.put("version", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_VERSION);
    properties.put("deploymentId", ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
    properties.put("tenantId", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_TENANT_ID);
  }
  
  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected RepositoryService repositoryService;
  
  @RequestMapping(value="/repository/process-definitions", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getProcessDefinitions(@RequestParam Map<String,String> allRequestParams, HttpServletRequest request) {
    ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
    
    // Populate filter-parameters
    if (allRequestParams.containsKey("category")) {
      processDefinitionQuery.processDefinitionCategory(allRequestParams.get("category"));
    }
    if (allRequestParams.containsKey("categoryLike")) {
      processDefinitionQuery.processDefinitionCategoryLike(allRequestParams.get("categoryLike"));
    }
    if (allRequestParams.containsKey("categoryNotEquals")) {
      processDefinitionQuery.processDefinitionCategoryNotEquals(allRequestParams.get("categoryNotEquals"));
    }
    if (allRequestParams.containsKey("key")) {
      processDefinitionQuery.processDefinitionKey(allRequestParams.get("key"));
    }
    if (allRequestParams.containsKey("keyLike")) {
      processDefinitionQuery.processDefinitionKeyLike(allRequestParams.get("keyLike"));
    }
    if (allRequestParams.containsKey("name")) {
      processDefinitionQuery.processDefinitionName(allRequestParams.get("name"));
    }
    if (allRequestParams.containsKey("nameLike")) {
      processDefinitionQuery.processDefinitionNameLike(allRequestParams.get("nameLike"));
    }
    if (allRequestParams.containsKey("resourceName")) {
      processDefinitionQuery.processDefinitionResourceName(allRequestParams.get("resourceName"));
    }
    if (allRequestParams.containsKey("resourceNameLike")) {
      processDefinitionQuery.processDefinitionResourceNameLike(allRequestParams.get("resourceNameLike"));
    }
    if (allRequestParams.containsKey("version")) {
      processDefinitionQuery.processDefinitionVersion(Integer.valueOf(allRequestParams.get("version")));
    }
    if (allRequestParams.containsKey("suspended")) {
      Boolean suspended = Boolean.valueOf(allRequestParams.get("suspended"));
      if (suspended != null) {
        if (suspended) {
          processDefinitionQuery.suspended();
        } else {
          processDefinitionQuery.active();
        }
      }
    }
    if (allRequestParams.containsKey("latest")) {
      Boolean latest = Boolean.valueOf(allRequestParams.get("latest"));
      if (latest != null && latest) {
        processDefinitionQuery.latestVersion();
      }
    }
    if (allRequestParams.containsKey("deploymentId")) {
      processDefinitionQuery.deploymentId(allRequestParams.get("deploymentId"));
    }
    if (allRequestParams.containsKey("startableByUser")) {
      processDefinitionQuery.startableByUser(allRequestParams.get("startableByUser"));
    }
    if (allRequestParams.containsKey("tenantId")) {
      processDefinitionQuery.processDefinitionTenantId(allRequestParams.get("tenantId"));
    }
    if (allRequestParams.containsKey("tenantIdLike")) {
      processDefinitionQuery.processDefinitionTenantIdLike(allRequestParams.get("tenantIdLike"));
    }
    
    return new ProcessDefinitionsPaginateList(restResponseFactory)
        .paginateList(allRequestParams, processDefinitionQuery, "name", properties);
  }
}
