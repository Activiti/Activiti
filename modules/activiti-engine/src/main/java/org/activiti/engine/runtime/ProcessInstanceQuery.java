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
package org.activiti.engine.runtime;

import java.util.List;

import org.activiti.engine.ActivitiException;



/** Allows programmatic querying of {@link ProcessInstance}s.
 * 
 * @author Joram Barrez
 */
public interface ProcessInstanceQuery {
  
  /** Only select the process instance with the given id */
  ProcessInstanceQuery processInstanceId(String processInstanceId);
  
  /** Only select the process instances which are defined by a process definition
   * with the given key.
   */
  ProcessInstanceQuery processDefinitionKey(String processDefinitionKey);
  
  /** Executes the query and get a list of {@link ProcessInstance}s as the result. */
  List<ProcessInstance> list();
  
  /** Executes the query and get a list of {@link ProcessInstance}s as the result. */
  List<ProcessInstance> listPage(int firstResult, int maxResults);
  
  /**
   * Executes the query and returns the {@link ProcessInstance}.
   * @throws ActivitiException when the query results in more 
   * than one process instance. 
   */
  ProcessInstance singleResult();
  
  /** Executes the query and returns the number of results */
  long count();
}
