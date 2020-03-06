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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.asyncexecutor.AcquiredTimerJobEntities;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;

/**

 */
public class AcquireTimerJobsCmd implements Command<AcquiredTimerJobEntities> {

  private final AsyncExecutor asyncExecutor;

  public AcquireTimerJobsCmd(AsyncExecutor asyncExecutor) {
    this.asyncExecutor = asyncExecutor;
  }

  public AcquiredTimerJobEntities execute(CommandContext commandContext) {
    AcquiredTimerJobEntities acquiredJobs = new AcquiredTimerJobEntities();
    List<TimerJobEntity> timerJobs = commandContext.getTimerJobEntityManager()
        .findTimerJobsToExecute(new Page(0, asyncExecutor.getMaxAsyncJobsDuePerAcquisition()));

    for (TimerJobEntity job : timerJobs) {
      lockJob(commandContext, job, asyncExecutor.getAsyncJobLockTimeInMillis());
      acquiredJobs.addJob(job);
    }

    return acquiredJobs;
  }

  protected void lockJob(CommandContext commandContext, TimerJobEntity job, int lockTimeInMillis) {
    
    // This will trigger an optimistic locking exception when two concurrent executors 
    // try to lock, as the revision will not match.
    
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
    job.setLockOwner(asyncExecutor.getLockOwner());
    job.setLockExpirationTime(gregorianCalendar.getTime());
  }
}