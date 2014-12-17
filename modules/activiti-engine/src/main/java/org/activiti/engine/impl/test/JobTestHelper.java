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


package org.activiti.engine.impl.test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.test.ActivitiRule;



/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 * @author Saeid Mirzaei
 */

// This helper class helps sharing the same code for jobExector test helpers, between Junit3 and junit 4 test support classes
public class JobTestHelper {

  public static void waitForJobExecutorToProcessAllJobs(ActivitiRule activitiRule, long maxMillisToWait, long intervalMillis) {
    waitForJobExecutorToProcessAllJobs(activitiRule.getProcessEngine().getProcessEngineConfiguration(), 
        activitiRule.getManagementService(), maxMillisToWait, intervalMillis);
  }
  
  public static void waitForJobExecutorToProcessAllJobs(ProcessEngineConfiguration processEngineConfiguration, 
      ManagementService managementService, long maxMillisToWait, long intervalMillis) {
   
  	waitForJobExecutorToProcessAllJobs(processEngineConfiguration, managementService, maxMillisToWait, intervalMillis, true);
   
  }

  public static void waitForJobExecutorToProcessAllJobs(ProcessEngineConfiguration processEngineConfiguration, 
      ManagementService managementService, long maxMillisToWait, long intervalMillis, boolean shutdownExecutorWhenFinished) {
    
    JobExecutor jobExecutor = null;
    AsyncExecutor asyncExecutor = null;
    if (processEngineConfiguration.isAsyncExecutorEnabled() == false) {
      jobExecutor = processEngineConfiguration.getJobExecutor();
      jobExecutor.start();
      
    } else {
      asyncExecutor = processEngineConfiguration.getAsyncExecutor();
      asyncExecutor.start();
    }

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          try {
            areJobsAvailable = areJobsAvailable(managementService);
          } catch(Throwable t) {
            // Ignore, possible that exception occurs due to locking/updating of table on MSSQL when
            // isolation level doesn't allow READ of the table
          }
        }
      } catch (InterruptedException e) {
        // ignore
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
    	if (shutdownExecutorWhenFinished) {
	      if (processEngineConfiguration.isAsyncExecutorEnabled() == false) {
	        jobExecutor.shutdown();
	      } else {
	        asyncExecutor.shutdown();
	      }
    	}
    }
  }

  public static void waitForJobExecutorOnCondition(ActivitiRule activitiRule, long maxMillisToWait, long intervalMillis, Callable<Boolean> condition) {
    waitForJobExecutorOnCondition(activitiRule.getProcessEngine().getProcessEngineConfiguration(), maxMillisToWait, intervalMillis, condition);
  }
  
  public static void waitForJobExecutorOnCondition(ProcessEngineConfiguration processEngineConfiguration, 
      long maxMillisToWait, long intervalMillis, Callable<Boolean> condition) {
    
    JobExecutor jobExecutor = null;
    AsyncExecutor asyncExecutor = null;
    if (processEngineConfiguration.isAsyncExecutorEnabled() == false) {
      jobExecutor = processEngineConfiguration.getJobExecutor();
      jobExecutor.start();
      
    } else {
      asyncExecutor = processEngineConfiguration.getAsyncExecutor();
      asyncExecutor.start();
    }

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean conditionIsViolated = true;
      try {
        while (conditionIsViolated) {
          Thread.sleep(intervalMillis);
          conditionIsViolated = !condition.call();
        }
      } catch (InterruptedException e) {
        // ignore
      } catch (Exception e) {
        throw new ActivitiException("Exception while waiting on condition: "+e.getMessage(), e);
      } finally {
        timer.cancel();
      }
      
      if (conditionIsViolated) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      if (processEngineConfiguration.isAsyncExecutorEnabled() == false) {
        jobExecutor.shutdown();
      } else {
        asyncExecutor.shutdown();
      }
    }
  }
  
  public static void executeJobExecutorForTime(ActivitiRule activitiRule, long maxMillisToWait, long intervalMillis) {
    executeJobExecutorForTime(activitiRule.getProcessEngine().getProcessEngineConfiguration(), maxMillisToWait, intervalMillis);
  }
  
  public static void executeJobExecutorForTime(ProcessEngineConfiguration processEngineConfiguration, long maxMillisToWait, long intervalMillis) {
    JobExecutor jobExecutor = null;
    AsyncExecutor asyncExecutor = null;
    if (processEngineConfiguration.isAsyncExecutorEnabled() == false) {
      jobExecutor = processEngineConfiguration.getJobExecutor();
      jobExecutor.start();
      
    } else {
      asyncExecutor = processEngineConfiguration.getAsyncExecutor();
      asyncExecutor.start();
    }

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      try {
        while (!task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
        }
      } catch (InterruptedException e) {
        // ignore
      } finally {
        timer.cancel();
      }

    } finally {
      if (processEngineConfiguration.isAsyncExecutorEnabled() == false) {
        jobExecutor.shutdown();
      } else {
        asyncExecutor.shutdown();
      }
    }
  }

  public static boolean areJobsAvailable(ActivitiRule activitiRule) {
    return areJobsAvailable(activitiRule.getManagementService());
    
  }

  public static boolean areJobsAvailable(ManagementService managementService) {
    return !managementService.createJobQuery().list().isEmpty();
  }

  private static class InteruptTask extends TimerTask {

    protected boolean timeLimitExceeded = false;
    protected Thread thread;

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