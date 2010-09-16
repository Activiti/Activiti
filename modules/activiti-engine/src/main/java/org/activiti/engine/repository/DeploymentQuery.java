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

package org.activiti.engine.repository;

import java.util.List;

import org.activiti.engine.ActivitiException;


/**
 * Allows programmatic querying of {@link Deployment}s.
 * 
 * @author Tom Baeyens
 */
public interface DeploymentQuery {

  String PROPERTY_ID = "ID_";
  String PROPERTY_NAME = "NAME_";
  String PROPERTY_DEPLOY_TIME = "DEPLOY_TIME_";
  
  /** Only select deployments with the given deployment id. */
  DeploymentQuery deploymentId(String deploymentId);
  
  /** Only select deployments with a name like the given string. */
  DeploymentQuery nameLike(String nameLike);
  
  /** Order the results ascending on the given property as
   * defined in this class. */
  DeploymentQuery orderAsc(String property);
  
  /** Order the results descending on the given property as
   * defined in this class. */
  DeploymentQuery orderDesc(String property);

  /** Executes the query and counts number of {@link Deployment}s in the result. */
  long count();
  
  /**
   * Executes the query and returns the {@link Deployment}. 
   * @throws ActivitiException when the query results in more 
   * than one deployment. 
   */
  Deployment singleResult();
  
  /** Executes the query and get a list of {@link Deployment}s as the result. */
  List<Deployment> list();
  
  /** Executes the query and get a list of {@link Deployment}s as the result. */
  List<Deployment> listPage(int firstResult, int maxResults);
}
