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
package org.activiti.impl.jobexecutor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.job.JobHandler;
import org.activiti.impl.job.JobHandlers;
import org.activiti.impl.job.JobImpl;


/**
 * @author Tom Baeyens
 */
public class ExecuteJobsCmd implements Command<Object> {

  private static Logger log = Logger.getLogger(ExecuteJobsCmd.class.getName());
  
  private final String jobId;
  private final JobHandlers jobHandlers;

  public ExecuteJobsCmd(JobHandlers jobHandlers, String jobId) {
    this.jobHandlers = jobHandlers;
    this.jobId = jobId;
  }

  public Object execute(CommandContext commandContext) {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Executing job " + jobId);
    }
    JobImpl job = commandContext.getPersistenceSession().findJobById(jobId);
    
    if (job == null) {
      throw new ActivitiException("No job found for jobId '" + jobId + "'");
    }
    JobHandler jobHandler = jobHandlers.getJobHandler(job.getJobHandlerType());
    job.execute(jobHandler, commandContext);
    return null;
  }
}
