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
package org.activiti.engine.impl.persistence.entity;

import java.util.Date;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.event.EventLogEntry;

/**
 * An event log entry can only be inserted (and maybe deleted).
 * 

 */
@Internal
public interface EventLogEntryEntity extends Entity, EventLogEntry {

  void setLogNumber(long logNumber);

  void setType(String type);

  void setProcessDefinitionId(String processDefinitionId);

  void setProcessInstanceId(String processInstanceId);

  void setExecutionId(String executionId);

  void setTaskId(String taskId);

  void setTimeStamp(Date timeStamp);

  void setUserId(String userId);

  void setData(byte[] data);

  String getLockOwner();

  void setLockOwner(String lockOwner);

  String getLockTime();

  void setLockTime(String lockTime);

  int getProcessed();

  void setProcessed(int isProcessed);

}
