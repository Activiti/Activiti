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

import org.activiti.engine.query.Query;


/** 
 * Programmatic querying for {@link HistoricDetail}s.
 * 
 * @author Tom Baeyens
 */
public interface HistoricDetailQuery extends Query<HistoricDetailQuery, HistoricDetail> {

  /** Only select historic variable updates with the given process instance.
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match. */
  HistoricDetailQuery processInstanceId(String processInstanceId);

  /** Only select historic variable updates associated to the given {@link HistoricActivityInstance activity instance}. 
   * @deprecated since 5.2, use {@link #activityInstanceId(String)} instead */
  HistoricDetailQuery activityId(String activityId);

  /** Only select historic variable updates associated to the given {@link HistoricActivityInstance activity instance}. */
  HistoricDetailQuery activityInstanceId(String activityInstanceId);

  /** Only select historic variable updates associated to the given {@link HistoricTaskInstance historic task instance}. */
  HistoricDetailQuery taskId(String taskId);

  /** Only select {@link HistoricFormProperty}s. */
  HistoricDetailQuery formProperties();

  /** Only select {@link HistoricVariableUpdate}s. */
  HistoricDetailQuery variableUpdates();

  HistoricDetailQuery orderByProcessInstanceId();
  
  HistoricDetailQuery orderByVariableName();
  
  HistoricDetailQuery orderByFormPropertyId();
  
  HistoricDetailQuery orderByVariableType();
  
  HistoricDetailQuery orderByVariableRevision();
  
  HistoricDetailQuery orderByTime();
}
