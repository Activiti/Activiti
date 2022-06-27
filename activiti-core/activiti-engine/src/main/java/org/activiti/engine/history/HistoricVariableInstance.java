/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.history;

import java.util.Date;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.history.HistoryLevel;

/**
 * A single process variable containing the last value when its process instance has finished. It is only available when HISTORY_LEVEL is set >= VARIABLE
 *
 */
@Internal
public interface HistoricVariableInstance extends HistoricData {

  /** The unique DB id */
  String getId();

  String getVariableName();

  String getVariableTypeName();

  Object getValue();

  /** The process instance reference. */
  String getProcessInstanceId();

  /**
   * @return the task id of the task, in case this variable instance has been set locally on a task. Returns null, if this variable is not related to a task.
   */
  String getTaskId();

  /**
   * Returns the time when the variable was created.
   */
  Date getCreateTime();

  /**
   * Returns the time when the value of the variable was last updated. Note that a {@link HistoricVariableInstance} only contains the latest value of the variable. The actual different value and value
   * changes are recorded in {@link HistoricVariableUpdate} instances, which are captured on {@link HistoryLevel} FULL.
   */
  Date getLastUpdatedTime();

}
