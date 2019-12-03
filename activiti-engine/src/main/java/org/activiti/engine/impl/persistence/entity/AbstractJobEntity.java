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
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.runtime.Job;

/**


 */
@Internal
public interface AbstractJobEntity extends Job, Entity, HasRevision {

  void setExecution(ExecutionEntity execution);

  void setDuedate(Date duedate);

  void setExecutionId(String executionId);

  void setRetries(int retries);

  void setProcessInstanceId(String processInstanceId);

  boolean isExclusive();

  void setExclusive(boolean isExclusive);

  void setProcessDefinitionId(String processDefinitionId);

  String getJobHandlerType();

  void setJobHandlerType(String jobHandlerType);

  String getJobHandlerConfiguration();

  void setJobHandlerConfiguration(String jobHandlerConfiguration);

  String getJobType();

  void setJobType(String jobType);
  
  String getRepeat();

  void setRepeat(String repeat);

  Date getEndDate();

  void setEndDate(Date endDate);

  int getMaxIterations();

  void setMaxIterations(int maxIterations);
  
  String getExceptionStacktrace();

  void setExceptionStacktrace(String exception);

  void setExceptionMessage(String exceptionMessage);

  ByteArrayRef getExceptionByteArrayRef();

  void setTenantId(String tenantId);

}
