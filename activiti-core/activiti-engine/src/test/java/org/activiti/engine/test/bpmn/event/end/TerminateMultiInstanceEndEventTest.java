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
package org.activiti.engine.test.bpmn.event.end;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**

 */
public class TerminateMultiInstanceEndEventTest extends PluggableActivitiTestCase {

  @Deployment
  public void testMultiInstanceEmbeddedSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMi");

    Task aTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(aTask.getId());

    List<Task> bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(bTasks).hasSize(8);

    // Complete 2 tasks by going to task C. The 3th tasks goes to the MI terminate end and shuts down the MI.
    for (int i=0; i<2; i++) {
      Task bTask = bTasks.get(i);
      taskService.complete(bTask.getId(), singletonMap("myVar", "toC"));
    }

    bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("B").list();
    assertThat(bTasks).hasSize(6);

    taskService.complete(bTasks.get(0).getId(), singletonMap("myVar", "toEnd"));

    Task afterMiTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(afterMiTask.getName()).isEqualTo("AfterMi");
    taskService.complete(afterMiTask.getId());

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testMultiInstanceEmbeddedSubprocessSequential() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMi");

    Task aTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(aTask.getId());

    List<Task> bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(bTasks).hasSize(1);
    taskService.complete(bTasks.get(0).getId(), singletonMap("myVar", "toC"));

    List<Task> cTasks = taskService.createTaskQuery().taskName("C").list();
    assertThat(cTasks).hasSize(1);
    taskService.complete(cTasks.get(0).getId());

    bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("B").list();
    assertThat(bTasks).hasSize(1);
    taskService.complete(bTasks.get(0).getId(), singletonMap("myVar", "toEnd"));

    Task afterMiTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(afterMiTask.getName()).isEqualTo("AfterMi");
    taskService.complete(afterMiTask.getId());

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testMultiInstanceEmbeddedSubprocess2() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMi");

    Task aTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(aTask.getId());

    List<Task> bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(bTasks).hasSize(5);

    // Complete one b task to get one C and D
    taskService.complete(bTasks.get(0).getId());

    // C and D should now be active
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertThat(tasks).hasSize(6);
    // 0-3 are B tasks
    assertThat(tasks.get(4).getName()).isEqualTo("C");
    assertThat(tasks.get(5).getName()).isEqualTo("D");

    // Completing C should terminate the multi instance
    taskService.complete(tasks.get(4).getId());

    Task afterMiTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(afterMiTask.getName()).isEqualTo("AfterMi");
    taskService.complete(afterMiTask.getId());

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testMultiInstanceEmbeddedSubprocess2Sequential() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMi");

    Task aTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(aTask.getId());

    List<Task> bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(bTasks).hasSize(1);

    // Complete one b task to get one C and D
    taskService.complete(bTasks.get(0).getId());

    // C and D should now be active
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertThat(tasks).hasSize(2);
    assertThat(tasks.get(0).getName()).isEqualTo("C");
    assertThat(tasks.get(1).getName()).isEqualTo("D");

    // Completing C should terminate the multi instance
    taskService.complete(tasks.get(0).getId());

    Task afterMiTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(afterMiTask.getName()).isEqualTo("AfterMi");
    taskService.complete(afterMiTask.getId());

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = {
      "org/activiti/engine/test/bpmn/event/end/TerminateMultiInstanceEndEventTest.testTerminateMiCallactivity-parentProcess.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/end/TerminateMultiInstanceEndEventTest.testTerminateMiCallactivity-calledProcess.bpmn20.xml"
  })
  public void testTerminateMiCallactivity() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMiCallActivity");

    Task taskA = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskA.getName()).isEqualTo("A");
    taskService.complete(taskA.getId());

    // After completing A, four B's should be active (due to the call activity)
    List<Task> bTasks = taskService.createTaskQuery().taskName("B").list();
    assertThat(bTasks).hasSize(4);

    // Compelting 3 B tasks, giving 3 C's and D's
    for (int i=0; i<3; i++) {
      taskService.complete(bTasks.get(i).getId());
    }

    List<Task> cTasks = taskService.createTaskQuery().taskName("C").list();
    assertThat(cTasks).hasSize(3);
    List<Task> dTasks = taskService.createTaskQuery().taskName("D").list();
    assertThat(dTasks).hasSize(3);

    // Completing one of the C tasks should terminate the whole multi instance
    taskService.complete(cTasks.get(0).getId());

    List<Task> afterMiTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertThat(afterMiTasks).hasSize(2);
    assertThat(afterMiTasks.get(0).getName()).isEqualTo("AfterMi");
    assertThat(afterMiTasks.get(1).getName()).isEqualTo("Parallel task");
  }

  @Deployment(resources = {
      "org/activiti/engine/test/bpmn/event/end/TerminateMultiInstanceEndEventTest.testTerminateMiCallactivity-parentProcessSequential.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/end/TerminateMultiInstanceEndEventTest.testTerminateMiCallactivity-calledProcess.bpmn20.xml"
  })
  public void testTerminateMiCallactivitySequential() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMiCallActivity");

    Task taskA = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskA.getName()).isEqualTo("A");
    taskService.complete(taskA.getId());

    List<Task> bTasks = taskService.createTaskQuery().taskName("B").list();
    assertThat(bTasks).hasSize(1);
    taskService.complete(bTasks.get(0).getId());

    List<Task> cTasks = taskService.createTaskQuery().taskName("C").list();
    assertThat(cTasks).hasSize(1);
    List<Task> dTasks = taskService.createTaskQuery().taskName("D").list();
    assertThat(dTasks).hasSize(1);

    // Completing one of the C tasks should terminate the whole multi instance
    taskService.complete(cTasks.get(0).getId());

    List<Task> afterMiTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertThat(afterMiTasks).hasSize(2);
    assertThat(afterMiTasks.get(0).getName()).isEqualTo("AfterMi");
    assertThat(afterMiTasks.get(1).getName()).isEqualTo("Parallel task");
  }

  @Deployment
  public void testTerminateNestedMiEmbeddedSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
        "terminateNestedMiEmbeddedSubprocess", singletonMap("var", "notEnd"));

    List<Task> aTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("A").list();
    assertThat(aTasks).hasSize(12);
    List<Task> bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("B").list();
    assertThat(bTasks).hasSize(72);

    // Completing a few B's will create a subprocess with some C's
    int nrOfBTasksCompleted = 3;
    for (int i=0; i<nrOfBTasksCompleted; i++) {
      taskService.complete(bTasks.get(i).getId());
    }

    bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("B").list();
    assertThat(bTasks).hasSize(72 - nrOfBTasksCompleted);

    // Firing the timer --> inner MI gets destroyed
    List<Job> timers = managementService.createTimerJobQuery().list();
    assertThat(timers).hasSize(nrOfBTasksCompleted);
    managementService.moveTimerToExecutableJob(timers.get(0).getId());
    managementService.executeJob(timers.get(0).getId());

    // We only comnpleted 3 B's. 3 other ones should be destroyed too (as one inner multi instance are 6 instances of B)
    bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("B").list();
    assertThat(bTasks).hasSize(66);

    // One of the inner multi instances should have been killed
    List<Task> afterInnerMiTasks = taskService.createTaskQuery().taskName("AfterInnerMi").list();
    assertThat(afterInnerMiTasks).hasSize(1);

    for (Task aTask : aTasks) {
      taskService.complete(aTask.getId());
    }

    // Finish
    List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    while (nextTasks != null && nextTasks.size() > 0) {
      taskService.complete(nextTasks.get(0).getId());
      nextTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    }

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = "org/activiti/engine/test/bpmn/event/end/TerminateMultiInstanceEndEventTest.testTerminateNestedMiEmbeddedSubprocess.bpmn20.xml")
  public void testTerminateNestedMiEmbeddedSubprocess2() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
        "terminateNestedMiEmbeddedSubprocess", singletonMap("var", "toEnd"));

    List<Task> aTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("A").list();
    assertThat(aTasks).hasSize(12);
    List<Task> afterInnerMiTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("AfterInnerMi").list();
    assertThat(afterInnerMiTasks).hasSize(12);

  }

  @Deployment
  public void testTerminateNestedMiEmbeddedSubprocessWithOneLoopCardinality() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
        "terminateNestedMiEmbeddedSubprocess", singletonMap("var", "notEnd"));

    List<Task> aTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("A").list();
    assertThat(aTasks).hasSize(1);
    List<Task> bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("B").list();
    assertThat(bTasks).hasSize(1);

    taskService.complete(bTasks.get(0).getId());
    bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("B").list();
    assertThat(bTasks).hasSize(0);

    // Firing the timer --> inner MI gets destroyed
    List<Job> timers = managementService.createTimerJobQuery().list();
    assertThat(timers).hasSize(1);
    managementService.moveTimerToExecutableJob(timers.get(0).getId());
    managementService.executeJob(timers.get(0).getId());

    bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("B").list();
    assertThat(bTasks).hasSize(0);

    // One of the inner multi instances should have been killed
    List<Task> afterInnerMiTasks = taskService.createTaskQuery().taskName("AfterInnerMi").list();
    assertThat(afterInnerMiTasks).hasSize(1);

    for (Task aTask : aTasks) {
      taskService.complete(aTask.getId());
    }

    // Finish
    List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    while (nextTasks != null && nextTasks.size() > 0) {
      taskService.complete(nextTasks.get(0).getId());
      nextTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    }

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

}
