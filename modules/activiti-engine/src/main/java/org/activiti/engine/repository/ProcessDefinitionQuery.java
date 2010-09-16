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
import org.activiti.engine.runtime.ProcessInstance;


/**
 * Allows programmatic querying of {@link ProcessDefinition}s.
 * 
 * @author Tom Baeyens
 */
public interface ProcessDefinitionQuery {
  
  String PROPERTY_ID = "ID_";
  String PROPERTY_KEY = "KEY_";
  String PROPERTY_VERSION = "VERSION_";

  /** Only select process definitions that are deployed in 
   * deployment with the given deployment id 
   */
  ProcessDefinitionQuery deploymentId(String deploymentId);
  
  /** Order the results ascending on the given property as
   * defined in this class. */
  ProcessDefinitionQuery orderAsc(String property);
  
  /** Order the results descending on the given property as
   * defined in this class. */
  ProcessDefinitionQuery orderDesc(String property);

  /** Executes the query and counts number of {@link ProcessInstance}s in the result. */
  long count();
  
  /**
   * Executes the query and returns the {@link ProcessDefinition}.
   * @throws ActivitiException when the query results in more 
   * than one process definition. 
   */
  ProcessDefinition singleResult();
  
  /** Executes the query and get a list of {@link ProcessDefinition}s as the result. */
  List<ProcessDefinition> list();
  
  /** Executes the query and get a list of {@link ProcessDefinition}s as the result. */
  List<ProcessDefinition> listPage(int firstResult, int maxResults);
}
