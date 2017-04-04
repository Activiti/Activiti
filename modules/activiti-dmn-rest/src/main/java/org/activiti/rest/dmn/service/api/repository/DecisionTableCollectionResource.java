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
package org.activiti.rest.dmn.service.api.repository;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.dmn.api.DmnDecisionTableQuery;
import org.activiti.dmn.api.DmnRepositoryService;
import org.activiti.dmn.api.QueryProperty;
import org.activiti.dmn.engine.impl.DecisionTableQueryProperty;
import org.activiti.rest.dmn.common.DataResponse;
import org.activiti.rest.dmn.service.api.DmnRestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yvo Swillens
 */
@RestController
public class DecisionTableCollectionResource {

  private static final Map<String, QueryProperty> properties = new HashMap<>();

  static {
    properties.put("id", DecisionTableQueryProperty.DECISION_TABLE_ID);
    properties.put("key", DecisionTableQueryProperty.DECISION_TABLE_KEY);
    properties.put("category", DecisionTableQueryProperty.DECISION_TABLE_CATEGORY);
    properties.put("name", DecisionTableQueryProperty.DECISION_TABLE_NAME);
    properties.put("version", DecisionTableQueryProperty.DECISION_TABLE_VERSION);
    properties.put("deploymentId", DecisionTableQueryProperty.DEPLOYMENT_ID);
    properties.put("tenantId", DecisionTableQueryProperty.DECISION_TABLE_TENANT_ID);
  }

  @Autowired
  protected DmnRestResponseFactory dmnRestResponseFactory;

  @Autowired
  protected DmnRepositoryService dmnRepositoryService;

  @RequestMapping(value = "/dmn-repository/decision-tables", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getDecisionTables(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
    DmnDecisionTableQuery decisionTableQuery = dmnRepositoryService.createDecisionTableQuery();

    // Populate filter-parameters
    if (allRequestParams.containsKey("category")) {
      decisionTableQuery.decisionTableCategory(allRequestParams.get("category"));
    }
    if (allRequestParams.containsKey("categoryLike")) {
      decisionTableQuery.decisionTableCategoryLike(allRequestParams.get("categoryLike"));
    }
    if (allRequestParams.containsKey("categoryNotEquals")) {
      decisionTableQuery.decisionTableCategoryNotEquals(allRequestParams.get("categoryNotEquals"));
    }
    if (allRequestParams.containsKey("key")) {
      decisionTableQuery.decisionTableKey(allRequestParams.get("key"));
    }
    if (allRequestParams.containsKey("keyLike")) {
      decisionTableQuery.decisionTableKeyLike(allRequestParams.get("keyLike"));
    }
    if (allRequestParams.containsKey("name")) {
      decisionTableQuery.decisionTableName(allRequestParams.get("name"));
    }
    if (allRequestParams.containsKey("nameLike")) {
      decisionTableQuery.decisionTableNameLike(allRequestParams.get("nameLike"));
    }
    if (allRequestParams.containsKey("resourceName")) {
      decisionTableQuery.decisionTableResourceName(allRequestParams.get("resourceName"));
    }
    if (allRequestParams.containsKey("resourceNameLike")) {
      decisionTableQuery.decisionTableResourceNameLike(allRequestParams.get("resourceNameLike"));
    }
    if (allRequestParams.containsKey("version")) {
      decisionTableQuery.decisionTableVersion(Integer.valueOf(allRequestParams.get("version")));
    }

    if (allRequestParams.containsKey("latest")) {
      Boolean latest = Boolean.valueOf(allRequestParams.get("latest"));
      if (latest != null && latest) {
        decisionTableQuery.latestVersion();
      }
    }
    if (allRequestParams.containsKey("deploymentId")) {
      decisionTableQuery.deploymentId(allRequestParams.get("deploymentId"));
    }
    if (allRequestParams.containsKey("tenantId")) {
      decisionTableQuery.decisionTableTenantId(allRequestParams.get("tenantId"));
    }
    if (allRequestParams.containsKey("tenantIdLike")) {
      decisionTableQuery.decisionTableTenantIdLike(allRequestParams.get("tenantIdLike"));
    }

    return new DecisionTablesDmnPaginateList(dmnRestResponseFactory).paginateList(allRequestParams, decisionTableQuery, "name", properties);
  }
}
