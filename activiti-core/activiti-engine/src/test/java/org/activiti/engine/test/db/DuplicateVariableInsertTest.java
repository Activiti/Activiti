/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

package org.activiti.engine.test.db;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.impl.cmd.SetExecutionVariablesCmd;
import org.activiti.engine.impl.cmd.SetTaskVariablesCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

public class DuplicateVariableInsertTest extends PluggableActivitiTestCase {

  /**
   * Test for ACT-1887: Inserting the same new variable at the same time, from 2 different threads, using 2 modified commands that use a barrier for starting and a barrier for completing the command,
   * so they each insert a new variable guaranteed.
   */
  public void testDuplicateVariableInsertOnExecution() throws Exception {
    String processDefinitionId = deployOneTaskTestProcess();
    final ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId);

    final CyclicBarrier startBarrier = new CyclicBarrier(2);
    final CyclicBarrier endBarrier = new CyclicBarrier(2);

    final List<Exception> exceptions = new ArrayList<Exception>();

    Thread firstInsertThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          managementService.executeCommand(new SetVariableWithBarriersCommand(startBarrier, endBarrier, processInstance.getId()));
        } catch (Exception e) {
          exceptions.add(e);
        }
      }
    });

    Thread secondInsertThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          managementService.executeCommand(new SetVariableWithBarriersCommand(startBarrier, endBarrier, processInstance.getId()));
        } catch (Exception e) {
          exceptions.add(e);
        }
      }
    });

    firstInsertThread.start();
    secondInsertThread.start();

    // Wait for threads to complete
    firstInsertThread.join();
    secondInsertThread.join();

    // One of the 2 threads should get an optimistic lock exception
    assertThat(exceptions).hasSize(1);

    // One variable should be set
    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertThat(variables).hasSize(1);
    assertThat(variables.get("var")).isEqualTo("12345");
    runtimeService.deleteProcessInstance(processInstance.getId(), "ShouldNotFail");
  }

  /**
   * Test for ACT-1887: Inserting the same new variable at the same time, from 2 different threads, using 2 modified commands that use a barrier for starting and a barrier for completing the command,
   * so they each insert a new variable guaranteed.
   */
  public void testDuplicateVariableInsertOnTask() throws Exception {
    String processDefinitionId = deployOneTaskTestProcess();
    final ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId);
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    final CyclicBarrier startBarrier = new CyclicBarrier(2);
    final CyclicBarrier endBarrier = new CyclicBarrier(2);

    final List<Exception> exceptions = new ArrayList<Exception>();

    Thread firstInsertThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          managementService.executeCommand(new SetTaskVariableWithBarriersCommand(startBarrier, endBarrier, task.getId()));
        } catch (Exception e) {
          exceptions.add(e);
        }
      }
    });

    Thread secondInsertThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          managementService.executeCommand(new SetTaskVariableWithBarriersCommand(startBarrier, endBarrier, task.getId()));
        } catch (Exception e) {
          exceptions.add(e);
        }
      }
    });

    firstInsertThread.start();
    secondInsertThread.start();

    // Wait for threads to complete
    firstInsertThread.join();
    secondInsertThread.join();

    // One of the 2 threads should get an optimistic lock exception
    assertThat(exceptions).hasSize(1);
    assertThat(exceptions.get(0)).isInstanceOf(ActivitiOptimisticLockingException.class);

    // One variable should be set
    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertThat(variables).hasSize(1);
    assertThat(variables.get("var")).isEqualTo("12345");
    runtimeService.deleteProcessInstance(processInstance.getId(), "ShouldNotFail");
  }

  /**
   * Command wrapping a SetExecutionVariablesCmd, waiting in to start and end on the barriers passed in.
   *

   *
   */
  private class SetVariableWithBarriersCommand implements Command<Void> {

    private CyclicBarrier startBarrier;
    private CyclicBarrier endBarrier;
    private String executionId;

    public SetVariableWithBarriersCommand(CyclicBarrier startBarrier, CyclicBarrier endBarrier, String executionId) {
      this.startBarrier = startBarrier;
      this.endBarrier = endBarrier;
      this.executionId = executionId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
      try {
        startBarrier.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } catch (BrokenBarrierException e) {
        throw new RuntimeException(e);
      }

      new SetExecutionVariablesCmd(executionId, singletonMap("var", "12345"), false).execute(commandContext);

      try {
        endBarrier.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } catch (BrokenBarrierException e) {
        throw new RuntimeException(e);
      }
      return null;
    }
  }

  /**
   * Command wrapping a SetTaskVariablesCmd, waiting in to start and end on the barriers passed in.
   *

   *
   */
  private class SetTaskVariableWithBarriersCommand implements Command<Void> {

    private CyclicBarrier startBarrier;
    private CyclicBarrier endBarrier;
    private String taskId;

    public SetTaskVariableWithBarriersCommand(CyclicBarrier startBarrier, CyclicBarrier endBarrier, String taskId) {
      this.startBarrier = startBarrier;
      this.endBarrier = endBarrier;
      this.taskId = taskId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
      try {
        startBarrier.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } catch (BrokenBarrierException e) {
        throw new RuntimeException(e);
      }

      new SetTaskVariablesCmd(taskId, singletonMap("var", "12345"), false).execute(commandContext);

      try {
        endBarrier.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } catch (BrokenBarrierException e) {
        throw new RuntimeException(e);
      }
      return null;
    }
  }

}
