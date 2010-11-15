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

import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.query.Query;


/**
 * Programmatic querying for {@link HistoricActivityInstance}s.
 * 
 * @author Tom Baeyens
 */
public interface HistoricActivityInstanceQuery extends Query<HistoricActivityInstanceQuery, HistoricActivityInstance>{

  /** Only select historic activity instances with the given process instance.
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match. */
  HistoricActivityInstanceQuery processInstanceId(String processInstanceId);
  
  /** Only select historic activity instances for the given process definition */
  HistoricActivityInstanceQuery processDefinitionId(String processDefinitionId);

  /** Only select historic activity instances for the given execution */
  HistoricActivityInstanceQueryImpl executionId(String executionId);

  /** Only select historic activity instances for the given activiti */
  HistoricActivityInstanceQueryImpl activityId(String activityId);

  /** Only select historic activity instances for activities with the given name */
  HistoricActivityInstanceQueryImpl activityName(String activityName);

  /** Only select historic activity instances for activities with the given activity type */
  HistoricActivityInstanceQueryImpl activityType(String activityType);

  /** Only select historic activity instances for userTask activities assigned to the given user */
  HistoricActivityInstanceQueryImpl taskAssignee(String userId);
  
  /** Only select historic activity instances that are finished. */
  HistoricActivityInstanceQueryImpl finished();

  /** Only select historic activity instances that are not finished yet. */
  HistoricActivityInstanceQueryImpl unfinished();

  // ordering /////////////////////////////////////////////////////////////////
  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceId();

  /** Order by processInstanceId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByProcessInstanceId();

  /** Order by executionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByExecutionId();
  
  /** Order by activityId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByActivityId();
  
  /** Order by activityName (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByActivityName();
  
  /** Order by activityType (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByActivityType();
  
  /** Order by start (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceStartTime();
  
  /** Order by end (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceEndTime();
  
  /** Order by duration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceDuration();
  
  /** Order by processDefinitionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByProcessDefinitionId();
}
