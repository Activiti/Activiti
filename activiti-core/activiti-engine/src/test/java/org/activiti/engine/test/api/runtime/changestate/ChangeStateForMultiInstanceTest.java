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

package org.activiti.engine.test.api.runtime.changestate;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ChangeActivityStateBuilder;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.assertj.core.api.Condition;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static org.assertj.core.api.Assertions.assertThat;


public class ChangeStateForMultiInstanceTest extends PluggableActivitiTestCase {

    private ChangeStateEventListener changeStateEventListener = new ChangeStateEventListener();

    @Override
    protected void setUp() {
        processEngine.getRuntimeService().addEventListener(changeStateEventListener);
    }

    @Override
    protected void tearDown() {
        processEngine.getRuntimeService().removeEventListener(changeStateEventListener);
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceSequential.bpmn20.xml")
    public void testSetCurrentActivityToSequentialMultiInstanceTask() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("sequentialMultiInstance")
                .variable("nrOfLoops", 3)
                .start();

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactly("beforeMultiInstance");
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks)
                .extracting(Task::getTaskDefinitionKey)
                .containsExactly("beforeMultiInstance");

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("beforeMultiInstance", "seqTasks")
                .changeState();

        //First in the loop
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        //One MI root and one seq Execution
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactly("seqTasks", "seqTasks");

        Execution miRoot = executions.stream().filter(e -> ((ExecutionEntity) e).isMultiInstanceRoot()).findFirst().get();
        Map<String, Object> miRootVars = runtimeService.getVariables(miRoot.getId());
        assertThat(miRootVars)
                .extracting("nrOfActiveInstances", "nrOfCompletedInstances", "nrOfLoops")
                .containsExactly(1, 0, 3);

        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
        assertThat(tasks)
                .extracting(Task::getTaskDefinitionKey)
                .containsExactly("seqTasks");
        assertThat(taskService.getVariable(tasks.get(0).getId(), "loopCounter")).isEqualTo(0);

        taskService.complete(tasks.get(0).getId());

        //Second in the loop
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        //One MI root and one seq Execution
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactly("seqTasks", "seqTasks");

        miRoot = executions.stream().filter(e -> ((ExecutionEntity) e).isMultiInstanceRoot()).findFirst().get();
        miRootVars = runtimeService.getVariables(miRoot.getId());
        assertThat(miRootVars)
                .extracting("nrOfActiveInstances", "nrOfCompletedInstances", "nrOfLoops")
                .containsExactly(1, 1, 3);

        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
        assertThat(tasks)
                .extracting(Task::getTaskDefinitionKey)
                .containsExactly("seqTasks");
        assertThat(taskService.getVariable(tasks.get(0).getId(), "loopCounter")).isEqualTo(1);

        //Complete second and third
        taskService.complete(tasks.get(0).getId());
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
        taskService.complete(tasks.get(0).getId());

        //After the MI
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactly("nextTask");
        assertThat((ExecutionEntity) executions.get(0))
                .extracting(ExecutionEntity::isMultiInstanceRoot)
                .isEqualTo(false);

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().singleResult();
        assertThat(task)
                .extracting(Task::getTaskDefinitionKey)
                .isEqualTo("nextTask");
        assertThat(taskService.getVariable(task.getId(), "loopCounter")).isNull();

        //Complete the process
        taskService.complete(task.getId());
        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceSequential.bpmn20.xml")
    public void testSetCurrentActivityOfSequentialMultiInstanceTask() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("sequentialMultiInstance")
                .variable("nrOfLoops", 5)
                .start();

        completeTask(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult());

        List<Execution> seqExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(seqExecutions).hasSize(2);
        List<Task> activeSeqTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
        assertThat(activeSeqTasks).hasSize(1);

        //First in the loop
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("seqTasks");
        assertThat(taskService.getVariable(task.getId(), "loopCounter")).isEqualTo(0);
        taskService.complete(task.getId());

        //Second in the loop
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("seqTasks");
        assertThat(taskService.getVariable(task.getId(), "loopCounter")).isEqualTo(1);

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("seqTasks", "nextTask")
                .changeState();

        seqExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(seqExecutions).hasSize(1);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nextTask");
        assertThat(taskService.getVariable(task.getId(), "loopCounter")).isNull();
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceSequential.bpmn20.xml")
    public void testSetCurrentParentExecutionOfSequentialMultiInstanceTask() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("sequentialMultiInstance")
                .variable("nrOfLoops", 5)
                .start();

        completeTask(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult());

        List<Execution> seqExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(seqExecutions).hasSize(2);
        List<Task> activeSeqTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
        assertThat(activeSeqTasks).hasSize(1);

        //First in the loop
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("seqTasks");
        assertThat(taskService.getVariable(task.getId(), "loopCounter")).isEqualTo(0);
        taskService.complete(task.getId());

        //Second in the loop
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("seqTasks");
        assertThat(taskService.getVariable(task.getId(), "loopCounter")).isEqualTo(1);

        //move the parent execution - otherwise the parent multi instance execution remains, although active==false.
        String parentExecutionId = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult().getParentId();
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(parentExecutionId, "nextTask")
                .changeState();

        seqExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(seqExecutions).hasSize(1);
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nextTask");
        assertThat(taskService.getVariable(task.getId(), "loopCounter")).isNull();
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceParallel.bpmn20.xml")
    public void testSetCurrentActivityToParallelMultiInstanceTask() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("parallelMultiInstance")
                .variable("nrOfLoops", 3)
                .start();

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactly("beforeMultiInstance");
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks)
                .extracting(Task::getTaskDefinitionKey)
                .containsExactly("beforeMultiInstance");

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("beforeMultiInstance", "parallelTasks")
                .changeState();

        //First in the loop
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        //One MI root and 3 parallel Executions
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactly("parallelTasks", "parallelTasks", "parallelTasks", "parallelTasks");

        Execution miRoot = executions.stream().filter(e -> ((ExecutionEntity) e).isMultiInstanceRoot()).findFirst().get();
        Map<String, Object> miRootVars = runtimeService.getVariables(miRoot.getId());
        assertThat(miRootVars)
                .extracting("nrOfActiveInstances", "nrOfCompletedInstances", "nrOfLoops")
                .containsExactly(3, 0, 3);

        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
        assertThat(tasks)
                .extracting(Task::getTaskDefinitionKey)
                .containsExactly("parallelTasks", "parallelTasks", "parallelTasks");
        assertThat(tasks)
                .extracting(task -> taskService.getVariable(task.getId(), "loopCounter"))
                .isNotNull();
        //        assertThat(taskService.getVariable(tasks.get(0).getId(), "loopCounter")).isNotNull()

        //Complete one execution
        taskService.complete(tasks.get(0).getId());

        //Confirm new state
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactly("parallelTasks", "parallelTasks", "parallelTasks", "parallelTasks");

        miRoot = executions.stream().filter(e -> ((ExecutionEntity) e).isMultiInstanceRoot()).findFirst().get();
        miRootVars = runtimeService.getVariables(miRoot.getId());
        assertThat(miRootVars)
                .extracting("nrOfActiveInstances", "nrOfCompletedInstances", "nrOfLoops")
                .containsExactly(2, 1, 3);

        //Two executions are inactive, the completed before and the MI root
        assertThat(executions)
                .haveExactly(2, new Condition<>((Execution execution) -> !((ExecutionEntity) execution).isActive(), "inactive"));

        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
        assertThat(tasks)
                .extracting(Task::getTaskDefinitionKey)
                .containsExactly("parallelTasks", "parallelTasks");
        assertThat(taskService.getVariable(tasks.get(0).getId(), "loopCounter")).isEqualTo(1);

        //Complete the rest of the Tasks
        tasks.forEach(this::completeTask);

        //After the MI
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactly("nextTask");
        assertThat((ExecutionEntity) executions.get(0))
                .extracting(ExecutionEntity::isMultiInstanceRoot).isEqualTo(false);

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().singleResult();
        assertThat(task)
                .extracting(Task::getTaskDefinitionKey)
                .isEqualTo("nextTask");
        assertThat(taskService.getVariable(task.getId(), "loopCounter")).isNull();

        //Complete the process
        taskService.complete(task.getId());
        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceParallel.bpmn20.xml")
    public void testSetCurrentActivityOfParallelMultiInstanceTask() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("parallelMultiInstance")
                .variable("nrOfLoops", 3)
                .start();

        completeTask(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult());

        List<Execution> parallelExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(parallelExecutions).hasSize(4);
        List<Task> activeParallelTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
        assertThat(activeParallelTasks).hasSize(3);

        //Complete one of the tasks
        taskService.complete(activeParallelTasks.get(1).getId());
        parallelExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(parallelExecutions).hasSize(4);
        activeParallelTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
        assertThat(activeParallelTasks).hasSize(2);

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("parallelTasks", "nextTask")
                .changeState();

        parallelExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(parallelExecutions).hasSize(1);
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nextTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceParallel.bpmn20.xml")
    public void testSetCurrentParentExecutionOfParallelMultiInstanceTask() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("parallelMultiInstance")
                .variable("nrOfLoops", 3)
                .start();

        completeTask(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult());

        List<Execution> parallelExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(parallelExecutions).hasSize(4);
        List<Task> activeParallelTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
        assertThat(activeParallelTasks).hasSize(3);

        //Complete one of the tasks
        taskService.complete(activeParallelTasks.get(1).getId());
        parallelExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(parallelExecutions).hasSize(4);
        activeParallelTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list();
        assertThat(activeParallelTasks).hasSize(2);

        //Fetch the parent execution of the multi instance task execution
        String parentExecutionId = runtimeService.createExecutionQuery().executionId(activeParallelTasks.get(0).getExecutionId()).singleResult().getParentId();
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(parentExecutionId, "nextTask")
                .changeState();

        parallelExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(parallelExecutions).hasSize(1);
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nextTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceParallelSubProcess.bpmn20.xml")
    public void testSetCurrentExecutionWithinMultiInstanceParallelSubProcess() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("parallelMultiInstanceSubProcess")
                .variable("nrOfLoops", 3)
                .start();

        //One of the child executions is the parent of the multiInstance "loop"
        long executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(7);
        long parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(3);

        //Move one of the executions within the multiInstance subProcess
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(subTask1Executions.get(0).getId(), "subTask2")
                .changeState();

        executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(7);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(2);
        List<Execution> subTask2Executions = runtimeService.createExecutionQuery().activityId("subTask2").list();
        assertThat(subTask2Executions).hasSize(1);

        //Complete one of the parallel subProcesses "subTask2"
        Task task = taskService.createTaskQuery().executionId(subTask2Executions.get(0).getId()).singleResult();
        taskService.complete(task.getId());

        executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(5);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(2);
        subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(2);
        subTask2Executions = runtimeService.createExecutionQuery().activityId("subTask2").list();
        assertThat(subTask2Executions).isEmpty();

        //Move the other two executions, one by one
        ChangeActivityStateBuilder changeActivityStateBuilder = runtimeService.createChangeActivityStateBuilder();
        subTask1Executions.forEach(e -> changeActivityStateBuilder.moveExecutionToActivityId(e.getId(), "subTask2"));
        changeActivityStateBuilder.changeState();

        executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(5);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(2);
        subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).isEmpty();
        subTask2Executions = runtimeService.createExecutionQuery().activityId("subTask2").list();
        assertThat(subTask2Executions).hasSize(2);

        //Complete the rest of the SubProcesses
        List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("subTask2").list();
        assertThat(tasks).hasSize(2);
        tasks.forEach(t -> taskService.complete(t.getId()));

        //There's no multiInstance anymore
        executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(1);

        task = taskService.createTaskQuery().active().singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("lastTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceParallelSubProcess.bpmn20.xml")
    public void testSetCurrentActivityWithinMultiInstanceParallelSubProcess() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("parallelMultiInstanceSubProcess")
                .variable("nrOfLoops", 3)
                .start();

        //One of the child executions is the parent of the multiInstance "loop"
        long executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(7);
        long parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(3);

        //Move one of the executions within the multiInstance subProcess
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("subTask1", "subTask2")
                .changeState();

        executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(7);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).isEmpty();
        List<Execution> subTask2Executions = runtimeService.createExecutionQuery().activityId("subTask2").list();
        assertThat(subTask2Executions).hasSize(3);

        //Complete the parallel subProcesses "subTask2"
        List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("subTask2").list();
        assertThat(tasks).hasSize(3);
        tasks.forEach(t -> taskService.complete(t.getId()));

        //There's no multiInstance anymore
        executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(1);

        Task task = taskService.createTaskQuery().active().singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("lastTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceNestedParallelSubProcesses.bpmn20.xml")
    public void testSetCurrentExecutionWithinNestedMultiInstanceParallelSubProcess() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("parallelNestedMultiInstanceSubProcesses").start();

        long totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        long parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(3);

        //Start the nested subProcesses by completing the first task of the outer subProcess
        List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("subTask1").list();
        assertThat(tasks).hasSize(3);
        tasks.forEach(t -> taskService.complete(t.getId()));

        // 3 instances of the outerSubProcess and each have 3 instances of a nestedSubProcess, for a total of 9 nestedSubTask executions
        // 9 nestedSubProcess instances and 3 outerSubProcesses instances -> 12 executions
        // 1 Parent execution for the outerSubProcess and 1 parent for each nestedSubProcess -> 4 extra parent executions
        // Grand Total ->
        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(25);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(9);
        List<Execution> nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).hasSize(9);

        //Move one of the executions within of the nested multiInstance subProcesses
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(nestedSubTask1Executions.get(0).getId(), "nestedSubTask2")
                .moveExecutionToActivityId(nestedSubTask1Executions.get(3).getId(), "nestedSubTask2")
                .moveExecutionToActivityId(nestedSubTask1Executions.get(6).getId(), "nestedSubTask2")
                .changeState();

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(25);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(9);
        nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).hasSize(6);
        List<Execution> nestedSubTask2Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask2").list();
        assertThat(nestedSubTask2Executions).hasSize(3);

        //Complete one of the outer subProcesses
        Task task = taskService.createTaskQuery().executionId(nestedSubTask2Executions.get(0).getId()).singleResult();
        taskService.complete(task.getId());

        //One less task execution and one less nested instance
        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(23);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(8);
        nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).hasSize(6);
        nestedSubTask2Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask2").list();
        assertThat(nestedSubTask2Executions).hasSize(2);

        //Move the rest of the nestedSubTask1 executions
        ChangeActivityStateBuilder changeActivityStateBuilder = runtimeService.createChangeActivityStateBuilder();
        nestedSubTask1Executions.forEach(e -> changeActivityStateBuilder.moveExecutionToActivityId(e.getId(), "nestedSubTask2"));
        changeActivityStateBuilder.changeState();

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(23);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(8);
        nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).isEmpty();
        nestedSubTask2Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask2").list();
        assertThat(nestedSubTask2Executions).hasSize(8);

        //Complete all the nestedSubTask2
        tasks = taskService.createTaskQuery().taskDefinitionKey("nestedSubTask2").list();
        assertThat(tasks).hasSize(8);
        tasks.forEach(t -> taskService.complete(t.getId()));

        //Nested subProcesses have completed, only outer subProcess remain
        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask2Executions = runtimeService.createExecutionQuery().activityId("subTask2").list();
        assertThat(subTask2Executions).hasSize(3);

        //Complete the outer subProcesses
        tasks = taskService.createTaskQuery().taskDefinitionKey("subTask2").list();
        assertThat(tasks).hasSize(3);
        tasks.forEach(t -> taskService.complete(t.getId()));

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(1);

        task = taskService.createTaskQuery().active().singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("lastTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceNestedParallelSubProcesses.bpmn20.xml")
    public void testSetCurrentActivityWithinNestedMultiInstanceParallelSubProcess() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("parallelNestedMultiInstanceSubProcesses").start();

        long totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        long parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(3);

        //Start the nested subProcesses by completing the first task of the outer subProcess
        List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("subTask1").list();
        assertThat(tasks).hasSize(3);
        tasks.forEach(t -> taskService.complete(t.getId()));

        // 3 instances of the outerSubProcess and each have 3 instances of a nestedSubProcess, for a total of 9 nestedSubTask executions
        // 9 nestedSubProcess instances and 3 outerSubProcesses instances -> 12 executions
        // 1 Parent execution for the outerSubProcess and 1 parent for each nestedSubProcess -> 4 extra parent executions
        // Grand Total ->
        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(25);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(9);
        List<Execution> nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).hasSize(9);

        //Complete one task for each nestedSubProcess
        taskService.complete(taskService.createTaskQuery().executionId(nestedSubTask1Executions.get(0).getId()).singleResult().getId());
        taskService.complete(taskService.createTaskQuery().executionId(nestedSubTask1Executions.get(3).getId()).singleResult().getId());
        taskService.complete(taskService.createTaskQuery().executionId(nestedSubTask1Executions.get(6).getId()).singleResult().getId());

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(25);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(9);
        nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).hasSize(6);
        List<Execution> nestedSubTask2Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask2").list();
        assertThat(nestedSubTask2Executions).hasSize(3);

        //Moving the nestedSubTask1 activity should move all its executions
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("nestedSubTask1", "nestedSubTask2")
                .changeState();

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(25);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(9);
        nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).isEmpty();
        nestedSubTask2Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask2").list();
        assertThat(nestedSubTask2Executions).hasSize(9);

        //Complete all the nestedSubTask2
        tasks = taskService.createTaskQuery().taskDefinitionKey("nestedSubTask2").list();
        assertThat(tasks).hasSize(9);
        tasks.forEach(t -> taskService.complete(t.getId()));

        //Nested subProcesses have completed, only outer subProcess remain
        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask2Executions = runtimeService.createExecutionQuery().activityId("subTask2").list();
        assertThat(subTask2Executions).hasSize(3);

        //Complete the outer subProcesses
        tasks = taskService.createTaskQuery().taskDefinitionKey("subTask2").list();
        assertThat(tasks).hasSize(3);
        tasks.forEach(t -> taskService.complete(t.getId()));

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(1);

        Task task = taskService.createTaskQuery().active().singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("lastTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceParallelSubProcess.bpmn20.xml")
    public void testSetCurrentMultiInstanceSubProcessParentExecutionWithinProcess() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("parallelMultiInstanceSubProcess")
                .variable("nrOfLoops", 3)
                .start();

        //One of the child executions is the parent of the multiInstance "loop"
        long executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(7);
        long parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(3);

        //Complete one of the Tasks
        Task task = taskService.createTaskQuery().executionId(subTask1Executions.get(1).getId()).singleResult();
        taskService.complete(task.getId());

        executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(7);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(2);
        List<Execution> subTask2Executions = runtimeService.createExecutionQuery().activityId("subTask2").list();
        assertThat(subTask2Executions).hasSize(1);

        //Move the parallelSubProcess via the parentExecution Ids
        String ParallelSubProcessParentExecutionId = runtimeService.createExecutionQuery()
                .processInstanceId(processInstance.getId())
                .activityId("parallelSubProcess")
                .list()
                .stream()
                .findFirst()
                .map(Execution::getParentId)
                .get();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(ParallelSubProcessParentExecutionId, "lastTask")
                .changeState();

        //There's no multiInstance anymore
        executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(1);

        task = taskService.createTaskQuery().active().singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("lastTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceParallelSubProcess.bpmn20.xml")
    public void testSetCurrentMultiInstanceSubProcessParentActivityWithinProcess() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("parallelMultiInstanceSubProcess")
                .variable("nrOfLoops", 3)
                .start();

        //One of the child executions is the parent of the multiInstance "loop"
        long executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(7);
        long parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(3);

        //Complete one of the Tasks
        Task task = taskService.createTaskQuery().executionId(subTask1Executions.get(1).getId()).singleResult();
        taskService.complete(task.getId());

        executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(7);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(2);
        List<Execution> subTask2Executions = runtimeService.createExecutionQuery().activityId("subTask2").list();
        assertThat(subTask2Executions).hasSize(1);

        //Move the parallelSubProcess
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("parallelSubProcess", "lastTask")
                .changeState();

        //There's no multiInstance anymore
        executionsCount = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(executionsCount).isEqualTo(1);

        task = taskService.createTaskQuery().active().singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("lastTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceNestedParallelSubProcesses.bpmn20.xml")
    public void testSetCurrentMultiInstanceNestedSubProcessParentExecutionWithinSubProcess() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("parallelNestedMultiInstanceSubProcesses").start();

        long totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        long parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(3);

        //Start the nested subProcesses by completing the first task of the outer subProcess
        List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("subTask1").list();
        assertThat(tasks).hasSize(3);
        tasks.forEach(t -> taskService.complete(t.getId()));

        // 3 instances of the outerSubProcess and each have 3 instances of a nestedSubProcess, for a total of 9 nestedSubTask executions
        // 9 nestedSubProcess instances and 3 outerSubProcesses instances -> 12 executions
        // 1 Parent execution for the outerSubProcess and 1 parent for each nestedSubProcess -> 4 extra parent executions
        // Grand Total ->
        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(25);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(9);
        List<Execution> nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).hasSize(9);

        //Complete some of the Nested tasks
        taskService.complete(taskService.createTaskQuery().executionId(nestedSubTask1Executions.get(0).getId()).singleResult().getId());
        taskService.complete(taskService.createTaskQuery().executionId(nestedSubTask1Executions.get(3).getId()).singleResult().getId());
        taskService.complete(taskService.createTaskQuery().executionId(nestedSubTask1Executions.get(6).getId()).singleResult().getId());

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(25);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(9);
        nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).hasSize(6);
        List<Execution> nestedSubTask2Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask2").list();
        assertThat(nestedSubTask2Executions).hasSize(3);

        //Complete one of the nested subProcesses
        Task task = taskService.createTaskQuery().executionId(nestedSubTask2Executions.get(0).getId()).singleResult();
        taskService.complete(task.getId());

        //One less task execution and one less nested instance
        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(23);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(8);
        nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).hasSize(6);
        nestedSubTask2Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask2").list();
        assertThat(nestedSubTask2Executions).hasSize(2);

        //Move each nested multiInstance parent
        Stream<String> parallelNestedSubProcessesParentIds = runtimeService.createExecutionQuery()
                .processInstanceId(processInstance.getId())
                .activityId("parallelNestedSubProcess")
                .list()
                .stream()
                .map(Execution::getParentId)
                .distinct();

        parallelNestedSubProcessesParentIds.forEach(parentId -> {
            runtimeService.createChangeActivityStateBuilder()
                    .moveExecutionToActivityId(parentId, "subTask2")
                    .changeState();
        });

        //Nested subProcesses have completed, only outer subProcess remain
        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask2Executions = runtimeService.createExecutionQuery().activityId("subTask2").list();
        assertThat(subTask2Executions).hasSize(3);

        //Complete the outer subProcesses
        tasks = taskService.createTaskQuery().taskDefinitionKey("subTask2").list();
        assertThat(tasks).hasSize(3);
        tasks.forEach(t -> taskService.complete(t.getId()));

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(1);

        task = taskService.createTaskQuery().active().singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("lastTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = "org/activiti/engine/test/api/multiInstanceNestedParallelSubProcesses.bpmn20.xml")
    public void testSetCurrentMultiInstanceNestedSubProcessParentActivityWithinSubProcess() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("parallelNestedMultiInstanceSubProcesses").start();

        long totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        long parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask1Executions = runtimeService.createExecutionQuery().activityId("subTask1").list();
        assertThat(subTask1Executions).hasSize(3);

        //Start the nested subProcesses by completing the first task of the outer subProcess
        List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("subTask1").list();
        assertThat(tasks).hasSize(3);
        tasks.forEach(t -> taskService.complete(t.getId()));

        // 3 instances of the outerSubProcess and each have 3 instances of a nestedSubProcess, for a total of 9 nestedSubTask executions
        // 9 nestedSubProcess instances and 3 outerSubProcesses instances -> 12 executions
        // 1 Parent execution for the outerSubProcess and 1 parent for each nestedSubProcess -> 4 extra parent executions
        // Grand Total ->
        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(25);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(9);
        List<Execution> nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).hasSize(9);

        //Complete some of the Nested tasks
        taskService.complete(taskService.createTaskQuery().executionId(nestedSubTask1Executions.get(0).getId()).singleResult().getId());
        taskService.complete(taskService.createTaskQuery().executionId(nestedSubTask1Executions.get(3).getId()).singleResult().getId());
        taskService.complete(taskService.createTaskQuery().executionId(nestedSubTask1Executions.get(6).getId()).singleResult().getId());

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(25);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(9);
        nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).hasSize(6);
        List<Execution> nestedSubTask2Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask2").list();
        assertThat(nestedSubTask2Executions).hasSize(3);

        //Complete one of the nested subProcesses
        Task task = taskService.createTaskQuery().executionId(nestedSubTask2Executions.get(0).getId()).singleResult();
        taskService.complete(task.getId());

        //One less task execution and one less nested instance
        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(23);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelNestedSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(8);
        nestedSubTask1Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask1").list();
        assertThat(nestedSubTask1Executions).hasSize(6);
        nestedSubTask2Executions = runtimeService.createExecutionQuery().activityId("nestedSubTask2").list();
        assertThat(nestedSubTask2Executions).hasSize(2);

        //Move the activity nested multiInstance parent
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("parallelNestedSubProcess", "subTask2")
                .changeState();

        //Nested subProcesses have completed, only outer subProcess remain
        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcessOuter").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> subTask2Executions = runtimeService.createExecutionQuery().activityId("subTask2").list();
        assertThat(subTask2Executions).hasSize(3);

        //Complete the outer subProcesses
        tasks = taskService.createTaskQuery().taskDefinitionKey("subTask2").list();
        assertThat(tasks).hasSize(3);
        tasks.forEach(t -> taskService.complete(t.getId()));

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(1);

        task = taskService.createTaskQuery().active().singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("lastTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/parallelGatewayInsideMultiInstanceSubProcess.bpmn20.xml" })
    public void testSetCurrentActivitiesUsingParallelGatewayNestedInMultiInstanceSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGatewayInsideMultiInstanceSubProcess");

        long totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        long parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> preForkTaskExecutions = runtimeService.createExecutionQuery().activityId("preForkTask").list();
        assertThat(preForkTaskExecutions).hasSize(3);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).hasSize(3);
        assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("preForkTask");

        //Move a task before the fork within the multiInstance subProcess
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveSingleActivityIdToActivityIds("preForkTask", Arrays.asList("forkTask1", "forkTask2"))
                .changeState();

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(10);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> forkTask1Executions = runtimeService.createExecutionQuery().activityId("forkTask1").list();
        assertThat(forkTask1Executions).hasSize(3);
        List<Execution> forkTask2Executions = runtimeService.createExecutionQuery().activityId("forkTask2").list();
        assertThat(forkTask2Executions).hasSize(3);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).hasSize(6);
        Map<String, List<Task>> taskGroups = tasks.stream().collect(Collectors.groupingBy(Task::getTaskDefinitionKey));
        assertThat(taskGroups)
                .containsOnlyKeys("forkTask1", "forkTask2");
        assertThat(taskGroups.get("forkTask1")).hasSize(3);
        assertThat(taskGroups.get("forkTask2")).hasSize(3);

        //Move the parallel gateway task forward
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdsToSingleActivityId(Arrays.asList("forkTask1", "forkTask2"), "parallelJoin")
                .changeState();

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> postForkTaskExecutions = runtimeService.createExecutionQuery().activityId("postForkTask").list();
        assertThat(postForkTaskExecutions).hasSize(3);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).hasSize(3);
        tasks.forEach(t -> assertThat(t.getTaskDefinitionKey()).isEqualTo("postForkTask"));

        //Complete one of the tasks
        taskService.complete(tasks.get(1).getId());

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(5);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(2);
        postForkTaskExecutions = runtimeService.createExecutionQuery().activityId("postForkTask").list();
        assertThat(postForkTaskExecutions).hasSize(2);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).hasSize(2);
        tasks.forEach(t -> assertThat(t.getTaskDefinitionKey()).isEqualTo("postForkTask"));

        //Finish the rest since we cannot move out of a multiInstance subProcess
        tasks.forEach(t -> taskService.complete(t.getId()));

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(1);
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("lastTask");

        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/parallelGatewayInsideMultiInstanceSubProcess.bpmn20.xml" })
    public void testSetCurrentExecutionsUsingParallelGatewayNestedInMultiInstanceSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGatewayInsideMultiInstanceSubProcess");

        long totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        long parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> preForkTaskExecutions = runtimeService.createExecutionQuery().activityId("preForkTask").list();
        assertThat(preForkTaskExecutions).hasSize(3);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).hasSize(3);
        assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("preForkTask");

        //Move a task before the fork within the multiInstance subProcess
        preForkTaskExecutions.forEach(e -> runtimeService.createChangeActivityStateBuilder()
                .moveSingleExecutionToActivityIds(e.getId(), Arrays.asList("forkTask1", "forkTask2"))
                .changeState());

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(10);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> forkTask1Executions = runtimeService.createExecutionQuery().activityId("forkTask1").list();
        assertThat(forkTask1Executions).hasSize(3);
        List<Execution> forkTask2Executions = runtimeService.createExecutionQuery().activityId("forkTask2").list();
        assertThat(forkTask2Executions).hasSize(3);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).hasSize(6);
        Map<String, List<Task>> taskGroups = tasks.stream().collect(Collectors.groupingBy(Task::getTaskDefinitionKey));
        assertThat(taskGroups)
                .containsOnlyKeys("forkTask1", "forkTask2");
        assertThat(taskGroups.get("forkTask1")).hasSize(3);
        assertThat(taskGroups.get("forkTask2")).hasSize(3);

        //Move the parallel gateway task forward
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionsToSingleActivityId(
                        Stream.concat(forkTask1Executions.stream().map(Execution::getId), forkTask2Executions.stream().map(Execution::getId))
                                .collect(Collectors.toList()), "postForkTask")
                .changeState();

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(7);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(3);
        List<Execution> postForkTaskExecutions = runtimeService.createExecutionQuery().activityId("postForkTask").list();
        assertThat(postForkTaskExecutions).hasSize(3);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).hasSize(3);
        tasks.forEach(t -> assertThat(t.getTaskDefinitionKey()).isEqualTo("postForkTask"));

        //Complete one of the tasks
        taskService.complete(tasks.get(1).getId());

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(5);
        parallelSubProcessCount = runtimeService.createExecutionQuery().activityId("parallelSubProcess").count();
        assertThat(parallelSubProcessCount).isEqualTo(2);
        postForkTaskExecutions = runtimeService.createExecutionQuery().activityId("postForkTask").list();
        assertThat(postForkTaskExecutions).hasSize(2);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).hasSize(2);
        tasks.forEach(t -> assertThat(t.getTaskDefinitionKey()).isEqualTo("postForkTask"));

        //Finish the rest since we cannot move out of a multiInstance subProcess
        tasks.forEach(t -> taskService.complete(t.getId()));

        totalChildExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count();
        assertThat(totalChildExecutions).isEqualTo(1);
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("lastTask");

        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }

    protected void completeTask(Task task) {
        taskService.complete(task.getId());
    }

}
