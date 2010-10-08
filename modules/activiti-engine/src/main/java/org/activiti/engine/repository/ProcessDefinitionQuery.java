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
import org.activiti.engine.query.Query;

/**
 * Allows programmatic querying of {@link ProcessDefinition}s.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface ProcessDefinitionQuery extends Query<ProcessDefinitionQuery, ProcessDefinition> {
  
  /** Only select process definiton with the given id.  */
  ProcessDefinitionQuery id(String processDefinitionId);
  
  /** Only select process definitions with the given name. */
  ProcessDefinitionQuery name(String name);
  
  /**
   * Only select process definitions where the name matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  ProcessDefinitionQuery nameLike(String nameLike);

  /**
   * Only select process definitions that are deployed in a deployment with the
   * given deployment id
   */
  ProcessDefinitionQuery deploymentId(String deploymentId);

  /**
   * Only select process definition with the given key.
   */
  ProcessDefinitionQuery key(String key);

  /**
   * Only select process definitions where the key matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   */
  ProcessDefinitionQuery keyLike(String keyLike);
  
  /**
   * Only select process definition with a certain version.
   * Particulary useful when used in combination with {@link #key(String)}
   */
  ProcessDefinitionQuery version(Integer version);
  
  /**
   * Only select the process definitions which are the latest deployed
   * (ie. which have the highest version number for the given key).
   * 
   * Can only be used in combinatioin with {@link #key(String)} of {@link #keyLike(String)}.
   * Can also be used without any other criteria (ie. query.latest().list()), which
   * will then give all the latest versions of all the deployed process definitions.
   * 
   * @throws ActivitiException if used in combination with  {@link #id(string)}, {@link #version(int)}
   *                           or {@link #deploymentId(String)}
   */
  ProcessDefinitionQuery latest();

  // ordering ////////////////////////////////////////////////////////////
  
  /** Order by the id of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderById();
  
  /** Order by deployment id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByDeploymentId();
  
  /** Order by deployment key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByKey();
  
  /** Order by process definition version (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessDefinitionQuery orderByVersion();
}
