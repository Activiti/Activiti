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

import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cmd.TriggerCmd;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**

 */
public class OptimisticLockingExceptionTest extends PluggableActivitiTestCase {

  @Test
  @Deployment(resources = { "org/activiti/engine/test/concurrency/CompetingJoinTest.testCompetingJoins.bpmn20.xml" })
  public void testOptimisticLockExceptionForConcurrentJoin() throws Exception {

    // The optimistic locking exception should happen for this test to be useful.
    // But with concurrency, you never know. Hence why this test is repeated 10 time to make sure the chance for
    // the optimistic exception happening is as big as possible.

    boolean optimisticLockingExceptionHappenedOnce = false;

    for (int i=0; i<10; i++) {

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CompetingJoinsProcess");
      Execution execution1 = runtimeService.createExecutionQuery().activityId("wait1").processInstanceId(processInstance.getId()).singleResult();
      Execution execution2 = runtimeService.createExecutionQuery().activityId("wait2").processInstanceId(processInstance.getId()).singleResult();

      TestTriggerableThread t1 = new TestTriggerableThread(processEngine, execution1.getId());
      TestTriggerableThread t2 = new TestTriggerableThread(processEngine, execution2.getId());

      // Start the two trigger threads. They will wait at the barrier
      t1.start();
      t2.start();

      // Wait at the barrier, until all threads are at the barrier
      OptimisticLockingTestCommandContextCloseListener.TEST_BARRIER_BEFORE_CLOSE.await();

      long totalWaitTime = 0;
      while (t1.getException() == null && t2.getException() == null && runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count() == 1) {
        Thread.sleep(250L);
        totalWaitTime += 250L;

        if (totalWaitTime >= 5000L) {
          break;
        }
      }

      // Either the transactions just happened to be aligned perfectly and no problem occurred (process instance ended)
      // Or the process instance wasn't ended and one of the two threads has an exception


      // Optimistic locking exception happened, yay. We can stop the test.
      if ( (t1.getException() != null && t1.getException() instanceof ActivitiOptimisticLockingException)
          || (t2.getException() != null && t2.getException() instanceof ActivitiOptimisticLockingException)) {
        optimisticLockingExceptionHappenedOnce = true;
        break;
      }

      boolean processInstanceEnded = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count() == 0;
      assertThat(processInstanceEnded).isTrue();

    }

    assertThat(optimisticLockingExceptionHappenedOnce).isTrue();

  }

  /**
   * Test runnable that triggers an execution.
   */
  public static class TestTriggerableThread extends Thread {

    protected ProcessEngine processEngine;
    protected String executionId;
    protected Exception exception;

    public TestTriggerableThread(ProcessEngine processEngine, String executionid) {
      this.processEngine = processEngine;
      this.executionId = executionid;
    }

    public void run() {
      try {
        processEngine.getManagementService().executeCommand(new TestTriggerCommand(executionId, null));
      } catch (Exception e) {
        exception = e;
      }
    }

    public Exception getException() {
      return exception;
    }

    public void setException(Exception exception) {
      this.exception = exception;
    }

  }

  /**
   * Simple extension of the triggerCommand, that adds a {@link OptimisticLockingTestCommandContextCloseListener} to the current
   * {@link CommandContext} before doing the regular trigger.
   */
  public static class TestTriggerCommand extends TriggerCmd {

    public TestTriggerCommand(String executionId, Map<String, Object> processVariables) {
      super(executionId, processVariables);
    }

    @Override
    public Object execute(CommandContext commandContext) {
      commandContext.addCloseListener(new OptimisticLockingTestCommandContextCloseListener());
      return super.execute(commandContext);
    }

  }

  /**
   * {@link CommandContextCloseListener} that halts the closing of the {@link CommandContext} until all threads are synchronized.
   */
  public static class OptimisticLockingTestCommandContextCloseListener implements CommandContextCloseListener {

    public static CyclicBarrier TEST_BARRIER_BEFORE_CLOSE = new CyclicBarrier(3); // 2 threads for triggering the wait state, one for the unit test thread

    @Override
    public void closing(CommandContext commandContext) {
      try {
        TEST_BARRIER_BEFORE_CLOSE.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } catch (BrokenBarrierException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void closed(CommandContext commandContext) {

    }

    @Override
    public void afterSessionsFlush(CommandContext commandContext) {

    }

    @Override
    public void closeFailure(CommandContext commandContext) {

    }

  }


}
