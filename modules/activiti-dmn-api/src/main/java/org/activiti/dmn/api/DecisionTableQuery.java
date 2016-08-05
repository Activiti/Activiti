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

package org.activiti.dmn.api;

import java.util.Set;

/**
 * Allows programmatic querying of {@link DecisionTable}s.
 * 
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public interface DecisionTableQuery extends Query<DecisionTableQuery, DecisionTable> {

  /** Only select decision table with the given id. */
  DecisionTableQuery decisionTableId(String decisionTableId);
  
  /** Only select decision tables with the given ids. */
  DecisionTableQuery decisionTableIds(Set<String> decisionTableIds);
  
  /** Only select decision tables with the given category. */
  DecisionTableQuery decisionTableCategory(String decisionTableCategory);

  /**
   * Only select decision tables where the category matches the given parameter. The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  DecisionTableQuery decisionTableCategoryLike(String decisionTableCategoryLike);

  /**
   * Only select deployments that have a different category then the given one.
   * 
   * @see DeploymentBuilder#category(String)
   */
  DecisionTableQuery decisionTableCategoryNotEquals(String categoryNotEquals);

  /** Only select decision tables with the given name. */
  DecisionTableQuery decisionTableName(String decisionTableName);

  /**
   * Only select decision tables where the name matches the given parameter. The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  DecisionTableQuery decisionTableNameLike(String decisionTableNameLike);

  /**
   * Only select decision tables that are deployed in a deployment with the given deployment id
   */
  DecisionTableQuery deploymentId(String deploymentId);

  /**
   * Select decision tables that are deployed in deployments with the given set of ids
   */
  DecisionTableQuery deploymentIds(Set<String> deploymentIds);
  
  /**
   * Only select decision tables that are deployed in a deployment with the given parent deployment id
   */
  DecisionTableQuery parentDeploymentId(String parentDeploymentId);
  
  /**
   * Only select decision tables that are deployed in a deployment like the given parent deployment id
   */
  DecisionTableQuery parentDeploymentIdLike(String parentDeploymentIdLike);

  /**
   * Only select decision table with the given key.
   */
  DecisionTableQuery decisionTableKey(String decisionTableKey);

  /**
   * Only select decision tables where the key matches the given parameter. The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  DecisionTableQuery decisionTableKeyLike(String decisionTableKeyLike);

  /**
   * Only select process definition with a certain version. Particulary useful when used in combination with {@link #processDefinitionKey(String)}
   */
  DecisionTableQuery decisionTableVersion(Integer decisionTableVersion);
  
  /**
   * Only select decision tables which version are greater than a certain version.
   */
  DecisionTableQuery decisionTableVersionGreaterThan(Integer decisionTableVersion);
  
  /**
   * Only select decision tables which version are greater than or equals a certain version.
   */
  DecisionTableQuery decisionTableVersionGreaterThanOrEquals(Integer decisionTableVersion);
  
  /**
   * Only select decision tables which version are lower than a certain version.
   */
  DecisionTableQuery decisionTableVersionLowerThan(Integer decisionTableVersion);
  
  /**
   * Only select decision tables which version are lower than or equals a certain version.
   */
  DecisionTableQuery decisionTableVersionLowerThanOrEquals(Integer decisionTableVersion);

  /**
   * Only select the decision tables which are the latest deployed (ie. which have the highest version number for the given key).
   * 
   * Can also be used without any other criteria (ie. query.latest().list()),
   * which will then give all the latest versions of all the deployed decision tables.
   * 
   * @throws ActivitiIllegalArgumentException
   *           if used in combination with {@link #groupId(string)}, {@link #decisionTableVersion(int)} or {@link #deploymentId(String)}
   */
  DecisionTableQuery latestVersion();

  /** Only select decision table with the given resource name. */
  DecisionTableQuery decisionTableResourceName(String resourceName);

  /** Only select decision table with a resource name like the given . */
  DecisionTableQuery decisionTableResourceNameLike(String resourceNameLike);

  /**
   * Only select decision tables that have the given tenant id.
   */
  DecisionTableQuery decisionTableTenantId(String tenantId);

  /**
   * Only select decision tables with a tenant id like the given one.
   */
  DecisionTableQuery decisionTableTenantIdLike(String tenantIdLike);

  /**
   * Only select decision tables that do not have a tenant id.
   */
  DecisionTableQuery decisionTableWithoutTenantId();

  // ordering ////////////////////////////////////////////////////////////

  /**
   * Order by the category of the decision tables (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  DecisionTableQuery orderByDecisionTableCategory();

  /**
   * Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  DecisionTableQuery orderByDecisionTableKey();

  /**
   * Order by the id of the decision tables (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  DecisionTableQuery orderByDecisionTableId();

  /**
   * Order by the version of the decision tables (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  DecisionTableQuery orderByDecisionTableVersion();

  /**
   * Order by the name of the decision tables (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  DecisionTableQuery orderByDecisionTableName();

  /**
   * Order by deployment id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  DecisionTableQuery orderByDeploymentId();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  DecisionTableQuery orderByTenantId();

}
