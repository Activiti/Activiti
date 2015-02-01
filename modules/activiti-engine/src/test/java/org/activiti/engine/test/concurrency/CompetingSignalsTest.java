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
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.cfg.CommandExecutorImpl;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.RetryInterceptor;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 */
public class CompetingSignalsTest extends PluggableActivitiTestCase {

  private static Logger log = LoggerFactory.getLogger(CompetingSignalsTest.class);
  
  Thread testThread = Thread.currentThread();
  static ControllableThread activeThread;
  
  public class SignalThread extends ControllableThread {
    
    String executionId;
    ActivitiOptimisticLockingException exception;
    
    public SignalThread(String executionId) {
      this.executionId = executionId;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    public void run() {
      try {
        runtimeService.signal(executionId);
      } catch (ActivitiOptimisticLockingException e) {
        this.exception = e;
      }
      log.debug("{} ends", getName());
    }
  }
  
  public static class ControlledConcurrencyBehavior implements ActivityBehavior {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) throws Exception {
      activeThread.returnControlToTestThreadAndWait();
    }
  }
  
  @Deployment
  public void testCompetingSignals() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CompetingSignalsProcess");
    String processInstanceId = processInstance.getId();

    log.debug("test thread starts thread one");
    SignalThread threadOne = new SignalThread(processInstanceId);
    threadOne.startAndWaitUntilControlIsReturned();
    
    log.debug("test thread continues to start thread two");
    SignalThread threadTwo = new SignalThread(processInstanceId);
    threadTwo.startAndWaitUntilControlIsReturned();

    log.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    log.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);
    assertTextPresent("was updated by another transaction concurrently", threadTwo.exception.getMessage());
  }
  
  @Deployment(resources={"org/activiti/engine/test/concurrency/CompetingSignalsTest.testCompetingSignals.bpmn20.xml"})
  public void testCompetingSignalsWithRetry() throws Exception {
    RuntimeServiceImpl runtimeServiceImpl = (RuntimeServiceImpl)runtimeService;        
    CommandExecutorImpl before = (CommandExecutorImpl) runtimeServiceImpl.getCommandExecutor();
    try {
      CommandInterceptor retryInterceptor = new RetryInterceptor();
      retryInterceptor.setNext(before.getFirst());

      runtimeServiceImpl.setCommandExecutor(new CommandExecutorImpl(before.getDefaultConfig(), retryInterceptor));
      
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CompetingSignalsProcess");
      String processInstanceId = processInstance.getId();
  
      log.debug("test thread starts thread one");
      SignalThread threadOne = new SignalThread(processInstanceId);
      threadOne.startAndWaitUntilControlIsReturned();
      
      log.debug("test thread continues to start thread two");
      SignalThread threadTwo = new SignalThread(processInstanceId);
      threadTwo.startAndWaitUntilControlIsReturned();
  
      log.debug("test thread notifies thread 1");
      threadOne.proceedAndWaitTillDone();
      assertNull(threadOne.exception);
  
      log.debug("test thread notifies thread 2");
      threadTwo.proceedAndWaitTillDone();
      assertNull(threadTwo.exception);
    } finally {
      // restore the command executor
      runtimeServiceImpl.setCommandExecutor(before);
    }
    
  }
}
