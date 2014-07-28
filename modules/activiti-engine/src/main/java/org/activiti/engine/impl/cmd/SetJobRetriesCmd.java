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
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.runtime.Job;


/**
 * @author Falko Menge
 */
public class SetJobRetriesCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  private final String jobId;
  private final int retries;

  public SetJobRetriesCmd(String jobId, int retries) {
    if (jobId == null || jobId.length() < 1) {
      throw new ActivitiIllegalArgumentException("The job id is mandatory, but '" + jobId + "' has been provided.");
    }
    if (retries < 0) {
      throw new ActivitiIllegalArgumentException("The number of job retries must be a non-negative Integer, but '" + retries + "' has been provided.");
    }
    this.jobId = jobId;
    this.retries = retries;
  }

  public Void execute(CommandContext commandContext) {
    JobEntity job = commandContext
            .getJobEntityManager()
            .findJobById(jobId);
    if (job != null) {
      job.setRetries(retries);
      
      if(commandContext.getEventDispatcher().isEnabled()) {
      	commandContext.getEventDispatcher().dispatchEvent(
      			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, job));
      }
    } else {
      throw new ActivitiObjectNotFoundException("No job found with id '" + jobId + "'.", Job.class);
    }
    return null;
  }
}
