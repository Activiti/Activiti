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

package org.activiti.engine.history;

import java.util.List;

import org.activiti.engine.ActivitiException;



/** Allows programmatic querying of {@link HistoricProcessInstance}s.
 *  
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface HistoricProcessInstanceQuery {

  /** Only select historic process instances with the given process instance.
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match. */
  HistoricProcessInstanceQuery processInstanceId(String processInstanceId);
  
  /** Only select historic process instances for the given process definition */
  HistoricProcessInstanceQuery processDefinitionId(String processDefinitionId);
  
  /** Only select historic process instances with the given business key */
  HistoricProcessInstanceQuery businessKey(String businessKey);
  
  /** Only select historic process instance that are not yet finished. */
  HistoricProcessInstanceQuery open();
  
  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderById();
  
  /** Order by the process instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessInstanceId();
  
  /** Order by the process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByProcessDefinitionId();
  
  /** Order by the business key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByBusinessKey();

  /** Order by the start time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByStartTime();
  
  /** Order by the end time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByEndTime();
  
  /** Order by the duration of the process instance (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricProcessInstanceQuery orderByDuration();
  
  /** Order the results descending on the given property as
   * defined in this class (needs to come after a call to one of the orderByXxxx methods). */
  HistoricProcessInstanceQuery desc();
  
  /** Order the results descending on the given property as
   * defined in this class (needs to come after a call to one of the orderByXxxx methods). */
  HistoricProcessInstanceQuery asc();

  /** Executes the query and get a list of {@link HistoricProcessInstance}s as the result. */
  List<HistoricProcessInstance> list();
  
  /** Executes the query and get a page of {@link HistoricProcessInstance}s as the result. */
  List<HistoricProcessInstance> listPage(int firstResult, int maxResults);
  
  /** Executes the query and get the single expected result. 
   * @throws ActivitiException when the query results in more 
   * than one historic process instance. 
   */
  HistoricProcessInstance singleResult();

  /** Executes the query and get number of results. */
  long count();
}
