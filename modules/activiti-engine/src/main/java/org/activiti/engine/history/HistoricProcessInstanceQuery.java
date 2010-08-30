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



/** a programmatic query for {@link HistoricProcessInstance}s.
 *  
 * @author Tom Baeyens
 */
public interface HistoricProcessInstanceQuery {

  String PROPERTY_ID = "ID_";
  String PROPERTY_START = "START_TIME_";
  String PROPERTY_END = "END_TIME_";
  String PROPERTY_DURATION = "DURATION_";
  String PROPERTY_PROCESS_DEFINITION_ID = "PROC_DEF_ID_";

  /** only select historic process instances with the given process instance.
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match. */
  HistoricProcessInstanceQuery processInstanceId(String processInstanceId);
  
  /** only select historic process instances for the given process definition */
  HistoricProcessInstanceQuery processDefinitionId(String processDefinitionId);
  
  /** order the results ascending on the given property as
   * defined in this class. */
  HistoricProcessInstanceQuery orderAsc(String property);

  /** order the results descending on the given property as
   * defined in this class. */
  HistoricProcessInstanceQuery orderDesc(String property);

  /** executes the query and get a list of {@link HistoricProcessInstance}s as the result. */
  List<HistoricProcessInstance> list();
  
  /** executes the query and get a page of {@link HistoricProcessInstance}s as the result. */
  List<HistoricProcessInstance> listPage(int start, int maxResults);
  
  /** executes the query and get the single expected result. */
  HistoricProcessInstance singleResult();

  /** executes the query and get number of results. */
  long count();
}
