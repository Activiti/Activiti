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
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;


/**
 * Allows programmatic querying of {@link HistoricActivityInstance}s.
 * 
 * @author Tom Baeyens
 */
public interface HistoricActivityInstanceQuery {

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

  /** Only select historic activity instances for activities assigned to the given user */
  HistoricActivityInstanceQueryImpl assignee(String userId);

  /** Only select historic activity instances that are not finished yet. */
  HistoricActivityInstanceQueryImpl onlyOpen();

  // ordering /////////////////////////////////////////////////////////////////
  
  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderById();

  /** Order by start (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByStart();
  
  /** Order by end (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByEnd();
  
  /** Order by duration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByDuration();
  
  /** Order by executionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByExecutionId();
  
  /** Order by processDefinitionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByProcessDefinitionId();
  
  /** Order by processInstanceId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByProcessInstanceId();
  
  /** Order by the given property (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderBy(HistoricActivityInstanceQueryProperty property);

  /** Order the results ascending on the given property as
   * defined in this class (needs to come after a call to one of the orderByXxxx methods). */
  HistoricActivityInstanceQuery asc();

  /** Order the results descending on the given property as
   * defined in this class (needs to come after a call to one of the orderByXxxx methods). */
  HistoricActivityInstanceQuery desc();
  
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
