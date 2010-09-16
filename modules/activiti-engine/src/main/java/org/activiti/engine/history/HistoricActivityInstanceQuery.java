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


/**
 * Allows programmatic querying of {@link HistoricActivityInstance}s.
 * 
 * @author Tom Baeyens
 */
public interface HistoricActivityInstanceQuery {

  /** Only select historic process instances with the given process instance.
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match. */
  HistoricActivityInstanceQuery processInstanceId(String processInstanceId);
  
  /** Only select historic process instances for the given process definition */
  HistoricActivityInstanceQuery processDefinitionId(String processDefinitionId);
  
  HistoricActivityInstanceQuery orderById();
  HistoricActivityInstanceQuery orderByStart();
  HistoricActivityInstanceQuery orderByEnd();
  HistoricActivityInstanceQuery orderByDuration();
  HistoricActivityInstanceQuery orderByExecutionId();
  HistoricActivityInstanceQuery orderByProcessDefinitionId();
  HistoricActivityInstanceQuery orderByProcessInstanceId();
  
  HistoricActivityInstanceQuery orderBy(HistoricActivityInstanceQueryProperty property);

  /** Order the results ascending on the given property as
   * defined in this class. */
  HistoricActivityInstanceQuery asc();

  /** Order the results descending on the given property as
   * defined in this class. */
  HistoricActivityInstanceQuery desc();

  /** Order the results according to the given direction. */
  HistoricActivityInstanceQuery direction(Direction direction);
  
  /** Executes the query and get a list of {@link HistoricProcessInstance}s as the result. */
  List<HistoricActivityInstance> list();
  
  /** Executes the query and get a page of {@link HistoricProcessInstance}s as the result. */
  List<HistoricActivityInstance> listPage(int firstResult, int maxResults);
  
  /** Executes the query and get the single expected result. 
   * @throws ActivitiException when the query results in more 
   * than one historic activity instance.  
   */
  HistoricActivityInstance singleResult();

  /** Executes the query and get number of results. */
  long count();

}
