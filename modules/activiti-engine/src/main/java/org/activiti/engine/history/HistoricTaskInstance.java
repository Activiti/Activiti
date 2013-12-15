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

import java.util.Date;
import java.util.Map;


/**
 * Represents a historic task instance (waiting, finished or deleted) that is stored permanent for 
 * statistics, audit and other business intelligence purposes.
 * 
 * @author Tom Baeyens
 */
public interface HistoricTaskInstance {

  /** 
   * The unique identifier of this historic task instance. This is the same identifier as the
   * runtime Task instance.
   */
  String getId();

  /** Process definition reference. */
  String getProcessDefinitionId();

  /** Process instance reference. */
  String getProcessInstanceId();

  /** Execution reference. */
  String getExecutionId();

  /** The latest name given to this task. */
  String getName();

  /** The latest description given to this task. */
  String getDescription();

  /** The reason why this task was deleted {'completed' | 'deleted' | any other user defined string }. */
  String getDeleteReason();
  
  /** Task owner */
  String getOwner();

  /** The latest assignee given to this task. */
  String getAssignee();

  /** Time when the task started. */
  Date getStartTime();

  /** Time when the task was deleted or completed. */
  Date getEndTime();

  /** Difference between {@link #getEndTime()} and {@link #getStartTime()} in milliseconds.  */
  Long getDurationInMillis();
  
  /** Difference between {@link #getEndTime()} and {@link #getClaimTime()} in milliseconds.  */
  Long getWorkTimeInMillis();
  
  /** Time when the task was claimed. */
  Date getClaimTime();
  
  /** Task definition key. */
  String getTaskDefinitionKey();
  
  /** Task form key. */
  String getFormKey();
  
  /** Task priority */
  int getPriority();
  
  /** Task due date */
  Date getDueDate();
  
  /** Task category */
  String getCategory();
  
  /** The parent task of this task, in case this task was a subtask */
  String getParentTaskId();

  /** Returns the local task variables if requested in the task query */
  Map<String, Object> getTaskLocalVariables();
  
  /** Returns the process variables if requested in the task query */
  Map<String, Object> getProcessVariables();
}
