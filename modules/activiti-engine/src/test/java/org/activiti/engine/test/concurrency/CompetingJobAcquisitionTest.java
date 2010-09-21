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

package org.activiti.engine.test.concurrency;

import java.util.logging.Logger;

import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.impl.cmd.AcquireJobsCmd;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.AcquiredJobs;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.pvm.activity.ActivityBehavior;
import org.activiti.pvm.activity.ActivityExecution;


/**
 * @author Tom Baeyens
 */
public class CompetingJobAcquisitionTest extends ActivitiInternalTestCase {

  private static Logger log = Logger.getLogger(CompetingSignalsTest.class.getName());
  
  Thread testThread = Thread.currentThread();
  static ControllableThread activeThread;
  static String jobId;
  
  public class JobAcquisitionThread extends ControllableThread {
    ActivitiOptimisticLockingException exception;
    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    public void run() {
      try {
        JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
        processEngineConfiguration
          .getCommandExecutor()
          .execute(new ControllableAcquireJobsCmd(jobExecutor));

      } catch (ActivitiOptimisticLockingException e) {
        this.exception = e;
      }
      log.fine(getName()+" ends");
    }
  }
  
  public static class ControllableAcquireJobsCmd extends AcquireJobsCmd {
    public ControllableAcquireJobsCmd(JobExecutor jobExecutor) {
      super(jobExecutor);
    }
    @Override
    public AcquiredJobs execute(CommandContext commandContext) {
      AcquiredJobs acquiredJobs = super.execute(commandContext);
      
      activeThread.returnControlToTestThreadAndWait();
      
      return acquiredJobs;
    }
  }
  
  @Deployment
  public void testCompetingJobAcquisitions() throws Exception {
    runtimeService.startProcessInstanceByKey("CompetingJobAcquisitionProcess");

    log.fine("test thread starts thread one");
    JobAcquisitionThread threadOne = new JobAcquisitionThread();
    threadOne.startAndWaitUntilControlIsReturned();
    
    log.fine("test thread continues to start thread two");
    JobAcquisitionThread threadTwo = new JobAcquisitionThread();
    threadTwo.startAndWaitUntilControlIsReturned();

    log.fine("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    log.fine("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);
    assertTextPresent("was updated by another transaction concurrently", threadTwo.exception.getMessage());
  }

}
