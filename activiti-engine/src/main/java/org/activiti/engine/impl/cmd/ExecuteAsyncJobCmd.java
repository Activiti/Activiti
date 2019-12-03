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
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 */
public class ExecuteAsyncJobCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(ExecuteAsyncJobCmd.class);

  protected String jobId;

  public ExecuteAsyncJobCmd(String jobId) {
    this.jobId = jobId;
  }

  public Object execute(CommandContext commandContext) {

    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("jobId is null");
    }
    
    // We need to refetch the job, as it could have been deleted by another concurrent job
    // For exampel: an embedded subprocess with a couple of async tasks and a timer on the boundary of the subprocess
    // when the timer fires, all executions and thus also the jobs inside of the embedded subprocess are destroyed.
    // However, the async task jobs could already have been fetched and put in the queue.... while in reality they have been deleted. 
    // A refetch is thus needed here to be sure that it exists for this transaction.
    
    Job job = commandContext.getJobEntityManager().findById(jobId);
    if (job == null) {
      log.debug("Job does not exist anymore and will not be executed. It has most likely been deleted "
          + "as part of another concurrent part of the process instance.");
      return null;
    }

    if (log.isDebugEnabled()) {
      log.debug("Executing async job {}", job.getId());
    }

    commandContext.getJobManager().execute(job);

    if (commandContext.getEventDispatcher().isEnabled()) {
      commandContext.getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_EXECUTION_SUCCESS, job));
    }

    return null;
  }
}