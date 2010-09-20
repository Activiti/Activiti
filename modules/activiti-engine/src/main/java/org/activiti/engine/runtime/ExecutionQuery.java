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
import org.activiti.engine.impl.runtime.ExecutionQueryProperty;



/** Allows programatic querying of {@link Execution}s.
 * 
 * @author Joram Barrez
 */
public interface ExecutionQuery {
  
  /** Only select executions which have the given process definition key. **/
  ExecutionQuery processDefinitionKey(String processDefinitionKey);
  
  /** Only select executions which have the given process definition id. **/
  ExecutionQuery processDefinitionId(String processDefinitionId);
  
  /** Only select executions which have the given process instance id. **/
  ExecutionQuery processInstanceId(String processInstanceId);
  
  /** Only select executions with the given id. **/
  ExecutionQuery executionId(String executionId);
  
  /** Only select executions which contain an activity with the given id. **/
  ExecutionQuery activityId(String activityId);

  //ordering //////////////////////////////////////////////////////////////
  
  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ExecutionQuery orderByProcessInstanceId();
  
  /** Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ExecutionQuery orderByProcessDefinitionKey();
  
  /** Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ExecutionQuery orderByProcessDefinitionId();
  
  /** Order by the given property (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ExecutionQuery orderBy(ExecutionQueryProperty property);
  
  /** Order the results ascending on the given property as
   * defined in this class (needs to come after a call to one of the orderByXxxx methods). */
  ExecutionQuery asc();

  /** Order the results descending on the given property as
   * defined in this class (needs to come after a call to one of the orderByXxxx methods). */
  ExecutionQuery desc();

  
  //results //////////////////////////////////////////////////////////////
  
  /** Executes the query and get a list of {@link Execution}s as the result. */
  List<Execution> list();
  
  /** Executes the query and get a list of {@link Execution}s as the result. */
  List<Execution> listPage(int firstResult, int maxResults);
  
  /**
   * Executes the query and returns the {@link Execution}.
   * @throws ActivitiException when the query results in more 
   * than one execution. 
   */
  Execution singleResult();
  
  /** Executes the query and gets the number of result */
  long count();
}
