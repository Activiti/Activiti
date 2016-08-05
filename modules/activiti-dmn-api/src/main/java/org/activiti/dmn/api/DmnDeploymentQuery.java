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

/**
 * Allows programmatic querying of {@link DmnDeployment}s.
 * 
 * Note that it is impossible to retrieve the deployment resources through the results of this operation, since that would cause a huge transfer of (possibly) unneeded bytes over the wire.
 * 
 * To retrieve the actual bytes of a deployment resource use the operations on the {@link RepositoryService#getDeploymentResourceNames(String)} and
 * {@link RepositoryService#getResourceAsStream(String, String)}
 * 
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public interface DmnDeploymentQuery extends Query<DmnDeploymentQuery, DmnDeployment> {

  /**
   * Only select deployments with the given deployment id.
   */
  DmnDeploymentQuery deploymentId(String deploymentId);

  /**
   * Only select deployments with the given name.
   */
  DmnDeploymentQuery deploymentName(String name);

  /**
   * Only select deployments with a name like the given string.
   */
  DmnDeploymentQuery deploymentNameLike(String nameLike);

  /**
   * Only select deployments with the given category.
   * 
   * @see DeploymentBuilder#category(String)
   */
  DmnDeploymentQuery deploymentCategory(String category);

  /**
   * Only select deployments that have a different category then the given one.
   * 
   * @see DeploymentBuilder#category(String)
   */
  DmnDeploymentQuery deploymentCategoryNotEquals(String categoryNotEquals);

  /**
   * Only select deployment that have the given tenant id.
   */
  DmnDeploymentQuery deploymentTenantId(String tenantId);

  /**
   * Only select deployments with a tenant id like the given one.
   */
  DmnDeploymentQuery deploymentTenantIdLike(String tenantIdLike);

  /**
   * Only select deployments that do not have a tenant id.
   */
  DmnDeploymentQuery deploymentWithoutTenantId();
  
  /**
   * Only select deployment that have the given parent deployment id.
   */
  DmnDeploymentQuery parentDeploymentId(String parentDeploymentId);

  /**
   * Only select deployments with a parent deployment id like the given one.
   */
  DmnDeploymentQuery parentDeploymentIdLike(String parentDeploymentIdLike);

  /** Only select deployments with the given process definition key. */
  DmnDeploymentQuery decisionTableKey(String key);

  /**
   * Only select deployments with a process definition key like the given string.
   */
  DmnDeploymentQuery decisionTableKeyLike(String keyLike);

  // sorting ////////////////////////////////////////////////////////

  /**
   * Order by deployment id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  DmnDeploymentQuery orderByDeploymentId();

  /**
   * Order by deployment name (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  DmnDeploymentQuery orderByDeploymentName();

  /**
   * Order by deployment time (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  DmnDeploymentQuery orderByDeploymenTime();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  DmnDeploymentQuery orderByTenantId();
}
