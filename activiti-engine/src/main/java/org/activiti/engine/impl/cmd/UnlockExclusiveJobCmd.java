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
package org.activiti.engine.impl.cmd;

import java.io.Serializable;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 */
public class UnlockExclusiveJobCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(UnlockExclusiveJobCmd.class);

  protected Job job;

  public UnlockExclusiveJobCmd(Job job) {
    this.job = job;
  }

  public Object execute(CommandContext commandContext) {

    if (job == null) {
      throw new ActivitiIllegalArgumentException("job is null");
    }

    if (log.isDebugEnabled()) {
      log.debug("Unlocking exclusive job {}", job.getId());
    }

    if (job.isExclusive()) {
      if (job.getProcessInstanceId() != null) {
        ExecutionEntity execution = commandContext.getExecutionEntityManager().findById(job.getProcessInstanceId());
        if (execution != null) {
          commandContext.getExecutionEntityManager().clearProcessInstanceLockTime(execution.getId());
        } 
      }
    }

    return null;
  }
}