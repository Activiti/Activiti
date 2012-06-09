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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.query.Query;

/**
 * Allows programmatic querying of {@link ProcessDefinition}s.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Saeid Mirzaei
 */
public interface ProcessDefinitionQuery extends Query<ProcessDefinitionQuery, ProcessDefinition> {
  
  /** Only select process definiton with the given id.  */
  ProcessDefinitionQuery processDefinitionId(String processDefinitionId);
  
  /** Only select process definitions with the given category. */
  ProcessDefinitionQuery processDefinitionCategory(String processDefinitionCategory);
  
  /**
   * Only select process definitions where the category matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  ProcessDefinitionQuery processDefinitionCategoryLike(String processDefinitionCategoryLike);

  /** Only select process definitions with the given name. */
  ProcessDefinitionQuery processDefinitionName(String processDefinitionName);
  
  /**
   * Only select process definitions where the name matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  ProcessDefinitionQuery processDefinitionNameLike(String processDefinitionNameLike);

  /**
   * Only select process definitions that are deployed in a deployment with the
   * given deployment id
   */
  ProcessDefinitionQuery deploymentId(String deploymentId);

  /**
   * Only select process definition with the given key.
   */
  ProcessDefinitionQuery processDefinitionKey(String processDefinitionKey);

  /**
   * Only select process definitions where the key matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  ProcessDefinitionQuery processDefinitionKeyLike(String processDefinitionKeyLike);
  
  /**
   * Only select process definition with a certain version.
   * Particulary useful when used in combination with {@link #processDefinitionKey(String)}
   */
  ProcessDefinitionQuery processDefinitionVersion(Integer processDefinitionVersion);
  
  /**
   * Only select the process definitions which are the latest deployed
   * (ie. which have the highest version number for the given key).
   * 
   * Can only be used in combinatioin with {@link #processDefinitionKey(String)} of {@link #processDefinitionKeyLike(String)}.
   * Can also be used without any other criteria (ie. query.latest().list()), which
   * will then give all the latest versions of all the deployed process definitions.
   * 
   * @throws ActivitiException if used in combination with  {@link #groupId(string)}, {@link #processDefinitionVersion(int)}
   *                           or {@link #deploymentId(String)}
   */
  ProcessDefinitionQuery latestVersion();
  
  /** Only select process definition with the given resource name. */
  ProcessDefinitionQuery processDefinitionResourceName(String resourceName);

  /** Only select process definition with a resource name like the given . */
  ProcessDefinitionQuery processDefinitionResourceNameLike(String resourceNameLike);
  
  /**
   * Only selects process definitions which given userId is authoriezed to start
   */
  ProcessDefinitionQuery startableByUser(String userId);

  /**
   * Only selects process definitions which are suspended
   */
  ProcessDefinitionQuery suspended();
  
  /**
   * Only selects process definitions which are active
   */
  ProcessDefinitionQuery active();
  
  // Support for event subscriptions /////////////////////////////////////
  
  /**
   * @see #messageEventSubscriptionName(String)
   */
  @Deprecated
  ProcessDefinitionQuery messageEventSubscription(String messageName);
  
  /**
   * Selects the single process definition which has a start message event 
   * with the messageName.
   */
  ProcessDefinitionQuery messageEventSubscriptionName(String messageName);

  // ordering ////////////////////////////////////////////////////////////
  
  /** Order by the category of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByProcessDefinitionCategory();
  
  /** Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByProcessDefinitionKey();

  /** Order by the id of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByProcessDefinitionId();
  
  /** Order by the version of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByProcessDefinitionVersion();
  
  /** Order by the name of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByProcessDefinitionName();
  
  /** Order by deployment id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByDeploymentId();
  
}
