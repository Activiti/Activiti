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


/**
 * @author Tom Baeyens
 */
public interface HistoricActivityInstanceQuery {

  /** only select historic process instances with the given process instance.
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match. */
  HistoricActivityInstanceQuery processInstanceId(String processInstanceId);
  
  /** only select historic process instances for the given process definition */
  HistoricActivityInstanceQuery processDefinitionId(String processDefinitionId);
  
  HistoricActivityInstanceQuery orderById();
  HistoricActivityInstanceQuery orderByStart();
  HistoricActivityInstanceQuery orderByEnd();
  HistoricActivityInstanceQuery orderByDuration();
  HistoricActivityInstanceQuery orderByExecutionId();
  HistoricActivityInstanceQuery orderByProcessDefinitionId();
  HistoricActivityInstanceQuery orderByProcessInstanceId();
  
  HistoricActivityInstanceQuery orderBy(HistoricActivityInstanceQueryProperty property);

  /** order the results ascending on the given property as
   * defined in this class. */
  HistoricActivityInstanceQuery asc();

  /** order the results descending on the given property as
   * defined in this class. */
  HistoricActivityInstanceQuery desc();

  /** order the results according to the given direction. */
  HistoricActivityInstanceQuery direction(Direction direction);
  
  /** executes the query and get a list of {@link HistoricProcessInstance}s as the result. */
  List<HistoricActivityInstance> list();
  
  /** executes the query and get a page of {@link HistoricProcessInstance}s as the result. */
  List<HistoricActivityInstance> listPage(int firstResult, int maxResults);
  
  /** executes the query and get the single expected result. */
  HistoricActivityInstance singleResult();

  /** executes the query and get number of results. */
  long count();

}
