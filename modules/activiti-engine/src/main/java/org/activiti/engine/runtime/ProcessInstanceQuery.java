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
import org.activiti.engine.impl.runtime.ProcessInstanceQueryProperty;

/**
 * Allows programmatic querying of {@link ProcessInstance}s.
 * 
 * @author Joram Barrez
 */
public interface ProcessInstanceQuery {

  /** Select the process instance with the given id */
  ProcessInstanceQuery processInstanceId(String processInstanceId);

  /**
   * Select the process instances which are defined by a process definition with
   * the given key.
   */
  ProcessInstanceQuery processDefinitionKey(String processDefinitionKey);

  /**
   * Selects the process instances which are defined by a process definition
   * with the given id.
   */
  ProcessInstanceQuery processDefinitionId(String processDefinitionId);

  /**
   * Select the process instances which are a sub process instance of the given
   * super process instance.
   */
  ProcessInstanceQuery superProcessInstance(String superProcessInstanceId);

  /**
   * Select the process instance that have as sub process instance the given
   * process instance. Note that there will always be maximum only <b>one</b>
   * such process instance that can be the result of this query.
   */
  ProcessInstanceQuery subProcessInstance(String subProcessInstanceId);

  
  //ordering /////////////////////////////////////////////////////////////////
  
  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessInstanceQuery orderByProcessInstanceId();
  
  /** Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessInstanceQuery orderByProcessDefinitionKey();
  
  /** Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessInstanceQuery orderByProcessDefinitionId();
  
  /** Order by the given property (needs to be followed by {@link #asc()} or {@link #desc()}). */
  ProcessInstanceQuery orderBy(ProcessInstanceQueryProperty property);
  
  /** Order the results ascending on the given property as
   * defined in this class (needs to come after a call to one of the orderByXxxx methods). */
  ProcessInstanceQuery asc();

  /** Order the results descending on the given property as
   * defined in this class (needs to come after a call to one of the orderByXxxx methods). */
  ProcessInstanceQuery desc();
  
  
  //results /////////////////////////////////////////////////////////////////

  /**
   * Executes the query and get a list of {@link ProcessInstance}s as the
   * result.
   */
  List<ProcessInstance> list();

  /**
   * Executes the query and get a list of {@link ProcessInstance}s as the
   * result.
   */
  List<ProcessInstance> listPage(int firstResult, int maxResults);

  /**
   * Executes the query and returns the {@link ProcessInstance}.
   * 
   * @throws ActivitiException
   *           when the query results in more than one process instance.
   */
  ProcessInstance singleResult();

  /** Executes the query and returns the number of results */
  long count();
}
