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

package org.activiti.test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.jobexecutor.JobExecutor;

/**
 * @author Nick Burch
 */
public class JobExecutorPoller {
  
  private static Logger log = Logger.getLogger(JobExecutorPoller.class.getName());

  private final JobExecutor jobExecutor;
  private final CommandExecutor commandExecutor;

  public JobExecutorPoller(JobExecutor jobExecutor, CommandExecutor commandExecutor) {
    this.jobExecutor = jobExecutor;
    this.commandExecutor = commandExecutor;
  }

  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    jobExecutor.start();

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          areJobsAvailable = areJobsAvailable();
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  private boolean areJobsAvailable() {
    Boolean areJobsAvailable = commandExecutor.execute(new Command<Boolean>() {
      public Boolean execute(CommandContext commandContext) {
        return !commandContext.getPersistenceSession().findNextJobsToExecute(1).isEmpty();
      }
    });
    log.info("Jobs available: "+areJobsAvailable);
    return areJobsAvailable;
  }

  private static class InteruptTask extends TimerTask {

    private boolean timeLimitExceeded = false;

    Thread thread;

    public InteruptTask(Thread thread) {
      this.thread = thread;
    }

    public boolean isTimeLimitExceeded() {
      return timeLimitExceeded;
    }

    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }

  }

}
