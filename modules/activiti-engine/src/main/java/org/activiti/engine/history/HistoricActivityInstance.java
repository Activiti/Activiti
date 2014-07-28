/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.history;

import java.util.Date;

/**
 * Represents one execution of an activity and it's stored permanent for statistics, audit and other business intelligence purposes.
 * 
 * @author Christian Stettler
 * @author Joram Barrez
 */
public interface HistoricActivityInstance extends HistoricData {

  /** The unique identifier of this historic activity instance. */
  String getId();

  /** The unique identifier of the activity in the process */
  String getActivityId();

  /** The display name for the activity */
  String getActivityName();

  /** The XML tag of the activity as in the process file */
  String getActivityType();

  /** Process definition reference */
  String getProcessDefinitionId();

  /** Process instance reference */
  String getProcessInstanceId();

  /** Execution reference */
  String getExecutionId();

  /** The corresponding task in case of task activity */
  String getTaskId();
  
  /** The called process instance in case of call activity */
  String getCalledProcessInstanceId();

  /** Assignee in case of user task activity */
  String getAssignee();

  /** Time when the activity instance started */
  Date getStartTime();

  /** Time when the activity instance ended */
  Date getEndTime();

  /** Difference between {@link #getEndTime()} and {@link #getStartTime()}.  */
  Long getDurationInMillis();
  
  /** Returns the tenant identifier for the historic activity */
  String getTenantId();
}
