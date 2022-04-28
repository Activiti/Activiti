/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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


package org.activiti.engine.impl.test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.test.ActivitiRule;
import org.awaitility.Awaitility;

/**



 */

// This helper class helps sharing the same code for jobExecutor test helpers,
// between Junit3 and junit 4 test support classes
public class JobTestHelper {

  public static void waitForJobExecutorToProcessAllJobs(ActivitiRule activitiRule, long maxMillisToWait) {
    waitForJobExecutorToProcessAllJobs(activitiRule.getProcessEngine().getProcessEngineConfiguration(), activitiRule.getManagementService(), maxMillisToWait);
  }

  public static void waitForJobExecutorToProcessAllJobs(ProcessEngineConfiguration processEngineConfiguration, ManagementService managementService, long maxMillisToWait) {
    waitForJobExecutorToProcessAllJobs(processEngineConfiguration, managementService, maxMillisToWait, true);
  }

  public static void waitForJobExecutorToProcessAllJobs(ProcessEngineConfiguration processEngineConfiguration, ManagementService managementService, long maxMillisToWait,
                                                        boolean shutdownExecutorWhenFinished) {
    AsyncExecutor asyncExecutor = processEngineConfiguration.getAsyncExecutor();
    asyncExecutor.start();

    Awaitility.await().atMost(maxMillisToWait, TimeUnit.MILLISECONDS).until(()-> !areJobsAvailable(managementService));

    boolean areJobsAvailable = areJobsAvailable(managementService);
    if (shutdownExecutorWhenFinished) {
        asyncExecutor.shutdown();
    }
    if (areJobsAvailable) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
    }
  }

  public static void waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(ProcessEngineConfiguration processEngineConfiguration, ManagementService managementService, long maxMillisToWait) {
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(processEngineConfiguration, managementService, maxMillisToWait, true);
  }

  public static void waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(ProcessEngineConfiguration processEngineConfiguration, ManagementService managementService, long maxMillisToWait,
                                                                              boolean shutdownExecutorWhenFinished) {

    AsyncExecutor asyncExecutor = processEngineConfiguration.getAsyncExecutor();
    asyncExecutor.start();
    processEngineConfiguration.setAsyncExecutorActivate(true);

    Awaitility.await().atMost(maxMillisToWait, TimeUnit.MILLISECONDS).until(()-> !areJobsOrExecutableTimersAvailable(managementService));

    boolean areJobsAvailable = areJobsOrExecutableTimersAvailable(managementService);
    if (shutdownExecutorWhenFinished) {
        processEngineConfiguration.setAsyncExecutorActivate(false);
        asyncExecutor.shutdown();
    }

    if (areJobsAvailable) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
    }
  }

  public static void waitForJobExecutorOnCondition(ActivitiRule activitiRule, long maxMillisToWait, Callable<Boolean> condition) {
    waitForJobExecutorOnCondition(activitiRule.getProcessEngine().getProcessEngineConfiguration(), maxMillisToWait, condition);
  }

  public static void waitForJobExecutorOnCondition(ProcessEngineConfiguration processEngineConfiguration, long maxMillisToWait, Callable<Boolean> condition) {
    AsyncExecutor asyncExecutor = processEngineConfiguration.getAsyncExecutor();
    asyncExecutor.start();

    Awaitility.await().atMost(maxMillisToWait, TimeUnit.MILLISECONDS).until(condition::call);

    asyncExecutor.shutdown();
    boolean conditionIsViolated;
    try {
        conditionIsViolated = !condition.call();
    } catch (Exception e) {
        throw new ActivitiException("Exception while waiting on condition: " + e.getMessage(), e);
    }
    if (conditionIsViolated) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
    }
  }

  public static void executeJobExecutorForTime(ActivitiRule activitiRule, long maxMillisToWait, long intervalMillis) {
    executeJobExecutorForTime(activitiRule.getProcessEngine().getProcessEngineConfiguration(), maxMillisToWait, intervalMillis);
  }

  public static void executeJobExecutorForTime(ProcessEngineConfiguration processEngineConfiguration, long maxMillisToWait, long intervalMillis) {
    AsyncExecutor asyncExecutor = processEngineConfiguration.getAsyncExecutor();
    asyncExecutor.start();

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
      asyncExecutor.shutdown();
    }
  }

  public static boolean areJobsAvailable(ActivitiRule activitiRule) {
    return areJobsAvailable(activitiRule.getManagementService());

  }

  public static boolean areJobsAvailable(ManagementService managementService) {
    return !managementService.createJobQuery().list().isEmpty();
  }

  public static boolean areJobsOrExecutableTimersAvailable(ManagementService managementService) {
    boolean emptyJobs = managementService.createJobQuery().list().isEmpty();
    if (emptyJobs) {
      return !managementService.createTimerJobQuery().executable().list().isEmpty();
    } else {
      return true;
    }
  }

  private static class InteruptTask extends TimerTask {

    protected boolean timeLimitExceeded;
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
