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
 * @author Tom Baeyens
 */
public interface HistoricTaskInstanceQuery  extends Query<HistoricTaskInstanceQuery, HistoricTaskInstance> {

  HistoricTaskInstanceQuery taskId(String taskId);
  
  HistoricTaskInstanceQuery processInstanceId(String processInstanceId);
  
  HistoricTaskInstanceQuery executionId(String executionId);
  
  HistoricTaskInstanceQuery processDefinitionId(String processDefinitionId);
  
  HistoricTaskInstanceQuery taskName(String taskName);
  
  HistoricTaskInstanceQuery taskNameLike(String taskNameLike);
  
  HistoricTaskInstanceQuery taskDescription(String taskDescription);
  
  HistoricTaskInstanceQuery taskDescriptionLike(String taskDescriptionLike);
  
  HistoricTaskInstanceQuery taskDeleteReason(String taskDeleteReason);
  
  HistoricTaskInstanceQuery taskDeleteReasonLike(String taskDeleteReasonLike);
  
  HistoricTaskInstanceQuery taskAssignee(String taskAssignee);
  
  HistoricTaskInstanceQuery taskAssigneeLike(String taskAssigneeLike);
  
  HistoricTaskInstanceQuery finished();
  
  HistoricTaskInstanceQuery unfinished();
  
  HistoricTaskInstanceQuery orderByHistoricActivityInstanceId();
  
  HistoricTaskInstanceQuery orderByProcessDefinitionId();
  
  HistoricTaskInstanceQuery orderByProcessInstanceId();

  HistoricTaskInstanceQuery orderByExecutionId();
  
  HistoricTaskInstanceQuery orderByHistoricTaskInstanceDuration();
  
  HistoricTaskInstanceQuery orderByHistoricTaskInstanceEndTime();
  
  HistoricTaskInstanceQuery orderByHistoricActivityInstanceStartTime();
  
  HistoricTaskInstanceQuery orderByTaskName();
  
  HistoricTaskInstanceQuery orderByTaskDescription();
  
  HistoricTaskInstanceQuery orderByDeleteReason();
}
