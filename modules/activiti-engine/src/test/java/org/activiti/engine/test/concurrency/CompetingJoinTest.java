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
import org.activiti.engine.impl.cmd.SignalCmd;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 */
public class CompetingJoinTest extends PluggableActivitiTestCase {

  private static Logger log = LoggerFactory.getLogger(CompetingJoinTest.class);
  
  Thread testThread = Thread.currentThread();
  static ControllableThread activeThread;
  static String jobId;
  
  public class SignalThread extends ControllableThread {
    String executionId;
    ActivitiOptimisticLockingException exception;
    public SignalThread(String executionId) {
      this.executionId = executionId;
    }
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    public void run() {
      try {
        processEngineConfiguration
          .getCommandExecutor()
          .execute(new ControlledCommand(activeThread, new SignalCmd(executionId, null, null,null)));

      } catch (ActivitiOptimisticLockingException e) {
        this.exception = e;
      }
      log.debug("{} ends", getName());
    }
  }
  
  @Deployment
  public void testCompetingJoins() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CompetingJoinsProcess");
    Execution execution1 = runtimeService
      .createExecutionQuery()
      .processInstanceId(processInstance.getId())
      .activityId("wait1")
      .singleResult();

    Execution execution2 = runtimeService
      .createExecutionQuery()
      .processInstanceId(processInstance.getId())
      .activityId("wait2")
      .singleResult();

    log.debug("test thread starts thread one");
    SignalThread threadOne = new SignalThread(execution1.getId());
    threadOne.startAndWaitUntilControlIsReturned();
    
    log.debug("test thread continues to start thread two");
    SignalThread threadTwo = new SignalThread(execution2.getId());
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
