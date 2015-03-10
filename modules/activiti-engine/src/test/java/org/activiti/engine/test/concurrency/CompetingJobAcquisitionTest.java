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

import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.impl.cmd.AcquireTimerJobsCmd;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 */
public class CompetingJobAcquisitionTest extends PluggableActivitiTestCase {

  private static Logger log = LoggerFactory.getLogger(CompetingJobAcquisitionTest.class);
  
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
        processEngineConfiguration
          .getCommandExecutor()
          .execute(new ControlledCommand(activeThread, new AcquireTimerJobsCmd("testLockOwner", 60000, 5)));

      } catch (ActivitiOptimisticLockingException e) {
        this.exception = e;
      }
      log.debug("{} ends", getName());
    }
  }
  
  @Deployment
  public void testCompetingJobAcquisitions() throws Exception {
    runtimeService.startProcessInstanceByKey("CompetingJobAcquisitionProcess");

    log.debug("test thread starts thread one");
    JobAcquisitionThread threadOne = new JobAcquisitionThread();
    threadOne.startAndWaitUntilControlIsReturned();
    
    log.debug("test thread continues to start thread two");
    JobAcquisitionThread threadTwo = new JobAcquisitionThread();
    threadTwo.startAndWaitUntilControlIsReturned();

    log.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    log.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    
    assertNotNull(threadTwo.exception);
    assertTextPresent("was updated by another transaction concurrently", threadTwo.exception.getMessage());
  }

}
