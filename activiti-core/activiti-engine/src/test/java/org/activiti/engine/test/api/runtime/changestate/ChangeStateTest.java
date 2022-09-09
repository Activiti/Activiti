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


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.delegate.event.*;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.*;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
    @author LoveMyOrange
 */
public class ChangeStateTest extends PluggableActivitiTestCase {

    private ChangeStateEventListener changeStateEventListener = new ChangeStateEventListener();

    @Override
    protected void setUp() {
        processEngine.getRuntimeService().addEventListener(changeStateEventListener);
    }

    @Override
    protected void tearDown() {
        processEngine.getRuntimeService().removeEventListener(changeStateEventListener);
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml" })
    public void testSetCurrentActivityBackwardForSimpleProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("secondTask", "firstTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();


        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("firstTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml" })
    public void testSetCurrentExecutionBackwardForSimpleProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "firstTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();

        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("firstTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml" })
    public void testSetCurrentActivityForwardForSimpleProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("firstTask", "secondTask")
                .changeState();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();

        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("secondTask");

        assertThat(iterator.hasNext()).isFalse();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml" })
    public void testSetCurrentExecutionForwardForSimpleProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "secondTask")
                .changeState();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();

        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("secondTask");

        assertThat(iterator.hasNext()).isFalse();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksProcessWithTimer.bpmn20.xml" })
    public void testSetCurrentActivityWithTimerForSimpleProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        Execution execution = runtimeService.createExecutionQuery().parentId(task.getExecutionId()).singleResult();
        managementService.createTimerJobQuery().executionId(execution.getId()).singleResult();

        assertThat(timerJob).isNotNull();

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("firstTask", "secondTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();

        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.JOB_CANCELED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");


        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("secondTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksProcessWithTimer.bpmn20.xml" })
    public void testSetCurrentExecutionWithTimerForSimpleProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "secondTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();

        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.JOB_CANCELED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("secondTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksProcessWithTimer.bpmn20.xml" })
    public void testSetCurrentActivityToActivityWithTimerForSimpleProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNull();

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("secondTask", "firstTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_SCHEDULED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("firstTask");


        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksProcessWithTimer.bpmn20.xml" })
    public void testSetCurrentExecutionToActivityWithTimerForSimpleProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNull();

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "firstTask")
                .changeState();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_SCHEDULED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("firstTask");



        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksProcessWithTimers.bpmn20.xml" })
    public void testSetCurrentActivityWithTimerToActivityWithTimerSimpleProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcessWithTimers");

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");
        Execution execution = runtimeService.createExecutionQuery().parentId(task.getExecutionId()).singleResult();
        Job timerJob1 = managementService.createTimerJobQuery().executionId(execution.getId()).singleResult();
        assertThat(timerJob1).isNotNull();

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("firstTask", "secondTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");
        Job timerJob2 = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob2).isNotNull();
        assertThat(timerJob1.getExecutionId()).isNotEqualTo(timerJob2.getExecutionId());

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();

        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.JOB_CANCELED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("firstTimerEvent");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_SCHEDULED);
        entityEvent = (ActivitiEntityEvent) event;
        timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("secondTimerEvent");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("secondTask");



        assertThat(iterator.hasNext()).isFalse();

        Job job = managementService.moveTimerToExecutableJob(timerJob2.getId());
        managementService.executeJob(job.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("thirdTask");

        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityOutOfSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("subTask", "taskBefore")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(2);

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();

        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("taskBefore");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcess.bpmn20.xml" })
    public void testSetCurrentExecutionOutOfSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "taskBefore")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(2);

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();

        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("taskBefore");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityIntoSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("taskBefore", "subTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();


        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(((ActivitiVariableEvent) event).getVariableName()).isEqualTo("name");
        assertThat(((ActivitiVariableEvent) event).getVariableValue()).isEqualTo("John");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcess.bpmn20.xml" })
    public void testSetCurrentExecutionIntoSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "subTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(((ActivitiVariableEvent) event).getVariableName()).isEqualTo("name");
        assertThat(((ActivitiVariableEvent) event).getVariableValue()).isEqualTo("John");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityIntoSubProcessWithModeledDataObject() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("taskBefore", "subTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        Execution subProcessExecution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("subProcess")
                .singleResult();
        assertThat(runtimeService.getVariableLocal(subProcessExecution.getId(), "name", String.class)).isNotNull();
        DataObject nameDataObject = runtimeService.getDataObjectLocal(subProcessExecution.getId(), "name");
        assertThat(nameDataObject).isNotNull();
        assertThat(nameDataObject.getValue()).isEqualTo("John");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();


        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(((ActivitiVariableEvent) event).getVariableName()).isEqualTo("name");
        assertThat(((ActivitiVariableEvent) event).getVariableValue()).isEqualTo("John");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcess.bpmn20.xml" })
    public void testSetCurrentExecutionIntoSubProcessWithModeledDataObject() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "subTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        Execution subProcessExecution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("subProcess")
                .singleResult();
        assertThat(runtimeService.getVariableLocal(subProcessExecution.getId(), "name", String.class)).isNotNull();

        DataObject nameDataObject = runtimeService.getDataObjectLocal(subProcessExecution.getId(), "name");
        assertThat(nameDataObject).isNotNull();
        assertThat(nameDataObject.getValue()).isEqualTo("John");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(((ActivitiVariableEvent) event).getVariableName()).isEqualTo("name");
        assertThat(((ActivitiVariableEvent) event).getVariableValue()).isEqualTo("John");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcessWithTimer.bpmn20.xml" })
    public void testSetCurrentActivityOutOfSubProcessWithTimer() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("subTask", "taskBefore")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(2);

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();


        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.JOB_CANCELED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");


        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("taskBefore");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcessWithTimer.bpmn20.xml" })
    public void testSetCurrentExecutionOutOfSubProcessWithTimer() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "taskBefore")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(2);

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();


        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.JOB_CANCELED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");


        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("taskBefore");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcessWithTimer.bpmn20.xml" })
    public void testSetCurrentActivityToTaskInSubProcessWithTimer() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("taskBefore", "subTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(4);

        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();


        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_SCHEDULED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcessWithTimer.bpmn20.xml" })
    public void testSetCurrentExecutionToTaskInSubProcessWithTimer() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "subTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(4);

        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();

        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_SCHEDULED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcessWithTimer.bpmn20.xml" })
    public void testSetCurrentActivityToTaskInSubProcessAndExecuteTimer() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("taskBefore", "subTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();

        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_SCHEDULED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");

        assertThat(iterator.hasNext()).isFalse();

        Job executableJob = managementService.moveTimerToExecutableJob(timerJob.getId());
        managementService.executeJob(executableJob.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskWithTimerInSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityOutOfSubProcessTaskWithTimer() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("subTask", "subTask2")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask2");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();

        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.JOB_CANCELED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");



        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask2");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskWithTimerInSubProcess.bpmn20.xml" })
    public void testSetCurrentExecutionOutOfSubProcessTaskWithTimer() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "subTask2")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask2");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();

        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.JOB_CANCELED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");



        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask2");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskWithTimerInSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityToTaskWithTimerInSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("taskBefore", "subTask")
                .changeState();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();

        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_SCHEDULED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");



        assertThat(iterator.hasNext()).isFalse();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(4);
        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask2");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskWithTimerInSubProcess.bpmn20.xml" })
    public void testSetCurrentExecutionToTaskWithTimerInSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "subTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(4);
        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();


        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_SCHEDULED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");



        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask2");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskWithTimerInSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityToTaskWithTimerInSubProcessAndExecuteTimer() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("taskBefore", "subTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(4);
        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(timerJob).isNotNull();

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();

        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_SCHEDULED);
        ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
        Job timer = (Job) entityEvent.getEntity();
        assertThat(getJobActivityId(timer)).isEqualTo("boundaryTimerEvent");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");



        assertThat(iterator.hasNext()).isFalse();

        Job executableTimerJob = managementService.moveTimerToExecutableJob(timerJob.getId());
        managementService.executeJob(executableTimerJob.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskNestedSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityIntoNestedSubProcessExecutionFromRoot() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("taskBefore", "nestedSubTask")
                .changeState();

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactlyInAnyOrder("subProcess", "nestedSubProcess", "nestedSubTask");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();


        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.oneTaskNestedSubProcessWithObject.bpmn20.xml" })
    public void testSetCurrentActivityIntoNestedSubProcessExecutionFromRootWithDataObject() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("taskBefore", "nestedSubTask")
                .changeState();

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactlyInAnyOrder("subProcess", "nestedSubProcess", "nestedSubTask");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        Execution nestedSubProcess = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("nestedSubProcess")
                .singleResult();
        assertThat(runtimeService.getVariableLocal(nestedSubProcess.getId(), "name", String.class)).isNotNull();
        DataObject nameDataObject = runtimeService.getDataObjectLocal(nestedSubProcess.getId(), "name");
        assertThat(nameDataObject).isNotNull();
        assertThat(nameDataObject.getValue()).isEqualTo("John");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(((ActivitiVariableEvent) event).getVariableName()).isEqualTo("name");
        assertThat(((ActivitiVariableEvent) event).getVariableValue()).isEqualTo("John");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskNestedSubProcess.bpmn20.xml" })
    public void testSetCurrentExecutionIntoNestedSubProcessExecutionFromRoot() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "nestedSubTask")
                .changeState();

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactlyInAnyOrder("subProcess", "nestedSubProcess", "nestedSubTask");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();

        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.oneTaskNestedSubProcessWithObject.bpmn20.xml" })
    public void testSetCurrentExecutionIntoNestedSubProcessExecutionFromRootWithDataObject() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "nestedSubTask")
                .changeState();

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactlyInAnyOrder("subProcess", "nestedSubProcess", "nestedSubTask");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        Execution nestedSubProcess = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("nestedSubProcess")
                .singleResult();
        assertThat(runtimeService.getVariableLocal(nestedSubProcess.getId(), "name", String.class)).isNotNull();
        DataObject nameDataObject = runtimeService.getDataObjectLocal(nestedSubProcess.getId(), "name");
        assertThat(nameDataObject).isNotNull();
        assertThat(nameDataObject.getValue()).isEqualTo("John");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();


        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(((ActivitiVariableEvent) event).getVariableName()).isEqualTo("name");
        assertThat(((ActivitiVariableEvent) event).getVariableValue()).isEqualTo("John");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskNestedSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityIntoNestedSubProcessExecutionFromOuter() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("subTask", "nestedSubTask")
                .changeState();

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactlyInAnyOrder("subProcess", "nestedSubProcess", "nestedSubTask");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();



        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.oneTaskNestedSubProcessWithObject.bpmn20.xml" })
    public void testSetCurrentActivityIntoNestedSubProcessExecutionFromOuterWithDataObject() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("subTask", "nestedSubTask")
                .changeState();

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactlyInAnyOrder("subProcess", "nestedSubProcess", "nestedSubTask");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        Execution nestedSubProcess = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("nestedSubProcess")
                .singleResult();
        assertThat(runtimeService.getVariableLocal(nestedSubProcess.getId(), "name", String.class)).isNotNull();
        DataObject nameDataObject = runtimeService.getDataObjectLocal(nestedSubProcess.getId(), "name");
        assertThat(nameDataObject).isNotNull();
        assertThat(nameDataObject.getValue()).isEqualTo("John");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();



        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(((ActivitiVariableEvent) event).getVariableName()).isEqualTo("name");
        assertThat(((ActivitiVariableEvent) event).getVariableValue()).isEqualTo("John");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskNestedSubProcess.bpmn20.xml" })
    public void testSetCurrentExecutionIntoNestedSubProcessExecutionFromOuter() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "nestedSubTask")
                .changeState();

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactlyInAnyOrder("subProcess", "nestedSubProcess", "nestedSubTask");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();

        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.oneTaskNestedSubProcessWithObject.bpmn20.xml" })
    public void testSetCurrentExecutionIntoNestedSubProcessExecutionFromOuterWithDataObject() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions).hasSize(2);

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "nestedSubTask")
                .changeState();

        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions)
                .extracting(Execution::getActivityId)
                .containsExactlyInAnyOrder("subProcess", "nestedSubProcess", "nestedSubTask");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        Execution nestedSubProcess = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("nestedSubProcess")
                .singleResult();
        assertThat(runtimeService.getVariableLocal(nestedSubProcess.getId(), "name", String.class)).isNotNull();
        DataObject nameDataObject = runtimeService.getDataObjectLocal(nestedSubProcess.getId(), "name");
        assertThat(nameDataObject).isNotNull();
        assertThat(nameDataObject.getValue()).isEqualTo("John");

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();



        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubProcess");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(((ActivitiVariableEvent) event).getVariableName()).isEqualTo("name");
        assertThat(((ActivitiVariableEvent) event).getVariableValue()).isEqualTo("John");

        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("nestedSubTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskNestedSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityOutOfNestedSubProcessExecution() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("nestedSubTask", "subTaskAfter")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();



        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTaskAfter");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskNestedSubProcess.bpmn20.xml" })
    public void testSetCurrentExecutionOutOfNestedSubProcessExecution() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "subTaskAfter")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();


        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTaskAfter");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskNestedSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityOutOfNestedSubProcessExecutionIntoContainingSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("nestedSubTask", "subTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();

        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskNestedSubProcess.bpmn20.xml" })
    public void testSetCurrentExecutionOutOfNestedSubProcessExecutionIntoContainingSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startNestedSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "subTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();

        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subTask");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("nestedSubTask");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTaskAfter");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/taskTwoSubProcesses.bpmn20.xml" })
    public void testSetCurrentActivityFromSubProcessToAnotherSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoSubProcesses");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subtask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("subtask", "subtask2")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subtask2");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();




        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess2");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subtask2");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/taskTwoSubProcesses.bpmn20.xml" })
    public void testSetCurrentExecutionFromSubProcessToAnotherSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoSubProcesses");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subtask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "subtask2")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subtask2");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        // Verify events
        Iterator<ActivitiEvent> iterator = changeStateEventListener.iterator();
        assertThat(iterator.hasNext()).isTrue();
        ActivitiEvent event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subProcess2");

        assertThat(iterator.hasNext()).isTrue();
        event = iterator.next();
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
        assertThat(((ActivitiActivityEvent) event).getActivityId()).isEqualTo("subtask2");

        assertThat(iterator.hasNext()).isFalse();

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityForSubProcessWithVariables() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("subTask", "taskBefore")
                .processVariable("processVar1", "test")
                .processVariable("processVar2", 10)
                .localVariable("taskBefore", "localVar1", "test2")
                .localVariable("taskBefore", "localVar2", 20)
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(2);

        Map<String, Object> processVariables = runtimeService.getVariables(processInstance.getId());
        assertThat(processVariables)
                .contains(
                        entry("processVar1", "test"),
                        entry("processVar2", 10)
                )
                .doesNotContainKeys("localVar1", "localVar2");

        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("taskBefore").singleResult();
        Map<String, Object> localVariables = runtimeService.getVariablesLocal(execution.getId());
        assertThat(localVariables)
                .contains(
                        entry("localVar1", "test2"),
                        entry("localVar2", 20)
                );

        // Verify events
        assertThat(changeStateEventListener.getEvents())
                .extracting(ActivitiEvent::getType)
                .containsExactly(
                        ActivitiEventType.VARIABLE_CREATED,
                        ActivitiEventType.VARIABLE_CREATED,
                        ActivitiEventType.VARIABLE_CREATED,
                        ActivitiEventType.VARIABLE_CREATED,
                        ActivitiEventType.ACTIVITY_STARTED
                );

        assertThat(changeStateEventListener.getEvents())
                .filteredOn(stateEvent -> stateEvent instanceof ActivitiVariableEvent)
                .extracting(
                        stateEvent -> ((ActivitiVariableEvent) stateEvent).getVariableName(),
                        stateEvent -> ((ActivitiVariableEvent) stateEvent).getVariableValue()
                )
                .containsExactlyInAnyOrder(
                        tuple("processVar1", "test"),
                        tuple("processVar2", 10),
                        tuple("localVar1", "test2"),
                        tuple("localVar2", 20)
                );

        assertThat(changeStateEventListener.getEvents())
                .filteredOn(stateEvent -> stateEvent instanceof ActivitiActivityEvent)
                .extracting(stateEvent -> ((ActivitiActivityEvent) stateEvent).getActivityId())
                .containsExactlyInAnyOrder(
                        "taskBefore"
                );

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcess.bpmn20.xml" })
    public void testSetCurrentExecutionForSubProcessWithVariables() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        changeStateEventListener.clear();

        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(task.getExecutionId(), "taskBefore")
                .processVariable("processVar1", "test")
                .processVariable("processVar2", 10)
                .localVariable("taskBefore", "localVar1", "test2")
                .localVariable("taskBefore", "localVar2", 20)
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskBefore");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(2);

        Map<String, Object> processVariables = runtimeService.getVariables(processInstance.getId());
        assertThat(processVariables)
                .contains(
                        entry("processVar1", "test"),
                        entry("processVar2", 10)
                )
                .doesNotContainKeys("localVar1", "localVar2");

        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("taskBefore").singleResult();
        Map<String, Object> localVariables = runtimeService.getVariablesLocal(execution.getId());
        assertThat(localVariables)
                .contains(
                        entry("localVar1", "test2"),
                        entry("localVar2", 20)
                );

        // Verify events
        assertThat(changeStateEventListener.getEvents())
                .extracting(ActivitiEvent::getType)
                .containsExactly(
                        ActivitiEventType.VARIABLE_CREATED,
                        ActivitiEventType.VARIABLE_CREATED,
                        ActivitiEventType.VARIABLE_CREATED,
                        ActivitiEventType.VARIABLE_CREATED,
                        ActivitiEventType.ACTIVITY_STARTED
                );

        assertThat(changeStateEventListener.getEvents())
                .filteredOn(stateEvent -> stateEvent instanceof ActivitiVariableEvent)
                .extracting(
                        stateEvent -> ((ActivitiVariableEvent) stateEvent).getVariableName(),
                        stateEvent -> ((ActivitiVariableEvent) stateEvent).getVariableValue()
                )
                .containsExactlyInAnyOrder(
                        tuple("processVar1", "test"),
                        tuple("processVar2", 10),
                        tuple("localVar1", "test2"),
                        tuple("localVar2", 20)
                );

        assertThat(changeStateEventListener.getEvents())
                .filteredOn(stateEvent -> stateEvent instanceof ActivitiActivityEvent)
                .extracting(stateEvent -> ((ActivitiActivityEvent) stateEvent).getActivityId())
                .containsExactlyInAnyOrder(
                        "taskBefore"
                );

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityInUnstartedSubProcessWithModeledDataObject() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("taskBefore", "subTask")
                .changeState();

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        Execution subProcessExecution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("subProcess")
                .singleResult();
        assertThat(runtimeService.getVariableLocal(subProcessExecution.getId(), "name", String.class)).isNotNull();

        DataObject nameDataObject = runtimeService.getDataObjectLocal(subProcessExecution.getId(), "name");
        assertThat(nameDataObject).isNotNull();
        assertThat(nameDataObject.getValue()).isEqualTo("John");

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/oneTaskSubProcess.bpmn20.xml" })
    public void testSetCurrentActivityInUnstartedSubProcessWithLocalVariableOnSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("taskBefore", "subTask")
                .localVariable("subProcess", "name", "Joe")
                .changeState();

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        assertThat(executions).hasSize(3);

        Execution subProcessExecution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("subProcess")
                .singleResult();
        assertThat(runtimeService.getVariableLocal(subProcessExecution.getId(), "name", String.class)).isNotNull();

        DataObject nameDataObject = runtimeService.getDataObjectLocal(subProcessExecution.getId(), "name");
        assertThat(nameDataObject).isNotNull();
        assertThat(nameDataObject.getValue()).isEqualTo("Joe");

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("taskAfter");
        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateSignalCatchEvent.bpmn20.xml" })
    public void testSetCurrentActivityToIntermediateSignalCatchEvent() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).containsOnlyKeys("beforeCatchEvent");
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("beforeCatchEvent")).hasSize(1);

        //Move to catchEvent
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("beforeCatchEvent", "intermediateCatchEvent")
                .changeState();

        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();

        //Trigger the event
        runtimeService.signalEventReceived("someSignal");

        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("afterCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("afterCatchEvent")).hasSize(1);


        //Complete the process
        taskService.complete(tasks.get(0).getId());
        assertProcessEnded(processInstance.getId());

    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateSignalCatchEvent.bpmn20.xml" })
    public void testSetCurrentExecutionToIntermediateSignalCatchEvent() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).containsOnlyKeys("beforeCatchEvent");
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("beforeCatchEvent")).hasSize(1);

        //Move to catchEvent
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(classifiedExecutions.get("beforeCatchEvent").get(0).getId(), "intermediateCatchEvent")
                .changeState();

        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();


        //Trigger the event
        runtimeService.signalEventReceived("someSignal");

        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("afterCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("afterCatchEvent")).hasSize(1);


        //Complete the process
        taskService.complete(tasks.get(0).getId());
        assertProcessEnded(processInstance.getId());

    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateSignalCatchEvent.bpmn20.xml" })
    public void testSetCurrentActivityFromIntermediateSignalCatchEvent() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("beforeCatchEvent");

        //Complete initial task
        taskService.complete(task.getId());

        //Process is waiting for event invocation
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();



        //Move back to the initial task
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("intermediateCatchEvent", "beforeCatchEvent")
                .changeState();

        //Process is in the initial state, no subscriptions exists
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("beforeCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks).hasSize(1);
        assertThat(classifiedTasks.get("beforeCatchEvent")).hasSize(1);


        //Complete the task once more
        taskService.complete(tasks.get(0).getId());

        //Process is waiting for signal again
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();


        //Move forward from the event catch
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("intermediateCatchEvent", "afterCatchEvent")
                .changeState();

        //Process is on the last task
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("afterCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("afterCatchEvent")).hasSize(1);


        //Complete the process
        taskService.complete(tasks.get(0).getId());
        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateSignalCatchEvent.bpmn20.xml" })
    public void testSetCurrentExecutionFromIntermediateSignalCatchEvent() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("beforeCatchEvent");

        //Complete initial task
        taskService.complete(task.getId());

        //Process is waiting for event invocation
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();


        //Move back to the initial task
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(classifiedExecutions.get("intermediateCatchEvent").get(0).getId(), "beforeCatchEvent")
                .changeState();

        //Process is in the initial state, no subscriptions exists
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("beforeCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks).hasSize(1);
        assertThat(classifiedTasks.get("beforeCatchEvent")).hasSize(1);


        //Complete the task once more
        taskService.complete(tasks.get(0).getId());

        //Process is waiting for signal again
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();


        //Move forward from the event catch
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(classifiedExecutions.get("intermediateCatchEvent").get(0).getId(), "afterCatchEvent")
                .changeState();

        //Process is on the last task
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("afterCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("afterCatchEvent")).hasSize(1);


        //Complete the process
        taskService.complete(tasks.get(0).getId());
        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateMessageCatchEvent.bpmn20.xml" })
    public void testSetCurrentActivityToIntermediateMessageCatchEvent() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).containsOnlyKeys("beforeCatchEvent");
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("beforeCatchEvent")).hasSize(1);

        //Move to catchEvent
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("beforeCatchEvent", "intermediateCatchEvent")
                .changeState();

        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();


        //Trigger the event
        String messageCatchingExecutionId = classifiedExecutions.get("intermediateCatchEvent").get(0).getId();
        runtimeService.messageEventReceived("someMessage", messageCatchingExecutionId);

        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("afterCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("afterCatchEvent")).hasSize(1);


        //Complete the process
        taskService.complete(tasks.get(0).getId());
        assertProcessEnded(processInstance.getId());

    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateMessageCatchEvent.bpmn20.xml" })
    public void testSetCurrentExecutionToIntermediateMessageCatchEvent() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).containsOnlyKeys("beforeCatchEvent");
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("beforeCatchEvent")).hasSize(1);

        //Move to catchEvent
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(classifiedExecutions.get("beforeCatchEvent").get(0).getId(), "intermediateCatchEvent")
                .changeState();

        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();


        //Trigger the event
        String messageCatchingExecutionId = classifiedExecutions.get("intermediateCatchEvent").get(0).getId();
        runtimeService.messageEventReceived("someMessage", messageCatchingExecutionId);

        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("afterCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("afterCatchEvent")).hasSize(1);


        //Complete the process
        taskService.complete(tasks.get(0).getId());
        assertProcessEnded(processInstance.getId());

    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateMessageCatchEvent.bpmn20.xml" })
    public void testSetCurrentActivityFromIntermediateMessageCatchEvent() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("beforeCatchEvent");

        //Complete initial task
        taskService.complete(task.getId());

        //Process is waiting for event invocation
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();


        //Move back to the initial task
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("intermediateCatchEvent", "beforeCatchEvent")
                .changeState();

        //Process is in the initial state, no subscriptions exists
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("beforeCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks).hasSize(1);
        assertThat(classifiedTasks.get("beforeCatchEvent")).hasSize(1);


        //Complete the task once more
        taskService.complete(tasks.get(0).getId());

        //Process is waiting for signal again
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();


        //Move forward from the event catch
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("intermediateCatchEvent", "afterCatchEvent")
                .changeState();

        //Process is on the last task
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("afterCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("afterCatchEvent")).hasSize(1);


        //Complete the process
        taskService.complete(tasks.get(0).getId());
        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateMessageCatchEvent.bpmn20.xml" })
    public void testSetCurrentExecutionFromIntermediateMessageCatchEvent() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("beforeCatchEvent");

        //Complete initial task
        taskService.complete(task.getId());

        //Process is waiting for event invocation
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();


        //Move back to the initial task
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(classifiedExecutions.get("intermediateCatchEvent").get(0).getId(), "beforeCatchEvent")
                .changeState();

        //Process is in the initial state, no subscriptions exists
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("beforeCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks).hasSize(1);
        assertThat(classifiedTasks.get("beforeCatchEvent")).hasSize(1);


        //Complete the task once more
        taskService.complete(tasks.get(0).getId());

        //Process is waiting for signal again
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertThat(tasks).isEmpty();


        //Move forward from the event catch
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(classifiedExecutions.get("intermediateCatchEvent").get(0).getId(), "afterCatchEvent")
                .changeState();

        //Process is on the last task
        executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("afterCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("afterCatchEvent")).hasSize(1);


        //Complete the process
        taskService.complete(tasks.get(0).getId());
        assertProcessEnded(processInstance.getId());
    }

    protected void checkInitialStateForMultipleProcessesWithSimpleEventCatch(Map<String, List<Execution>> executionsByProcessInstance) {
        executionsByProcessInstance.forEach((processId, executions) -> {
            Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
            assertThat(classifiedExecutions).containsOnlyKeys("beforeCatchEvent");
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processId).list();
            Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
            assertThat(classifiedTasks.get("beforeCatchEvent")).hasSize(1);

        });
    }

    protected void checkWaitStateForMultipleProcessesWithSimpleEventCatch(Map<String, List<Execution>> executionsByProcessInstance) {
        executionsByProcessInstance.forEach((processId, executions) -> {
            Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
            assertThat(classifiedExecutions).hasSize(1);
            assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processId).list();
            assertThat(tasks).isEmpty();


        });
    }

    protected void checkFinalStateForMultipleProcessesWithSimpleEventCatch(Map<String, List<Execution>> executionsByProcessInstance) {
        executionsByProcessInstance.forEach((processId, executions) -> {
            Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
            assertThat(classifiedExecutions).hasSize(1);
            assertThat(classifiedExecutions.get("afterCatchEvent")).hasSize(1);
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processId).list();
            Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
            assertThat(classifiedTasks.get("afterCatchEvent")).hasSize(1);

        });
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateSignalCatchEvent.bpmn20.xml" })
    public void testSetCurrentActivityToIntermediateCatchEventForMultipleProcessesTriggerSimultaneously() {
        ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");
        ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        List<Execution> allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        Map<String, List<Execution>> executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        checkInitialStateForMultipleProcessesWithSimpleEventCatch(executionsByProcessInstance);

        //Move both processes to the eventCatch
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance1.getId())
                .moveActivityIdTo("beforeCatchEvent", "intermediateCatchEvent")
                .changeState();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance2.getId())
                .moveActivityIdTo("beforeCatchEvent", "intermediateCatchEvent")
                .changeState();

        allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        checkWaitStateForMultipleProcessesWithSimpleEventCatch(executionsByProcessInstance);

        //Trigger signal
        runtimeService.signalEventReceived("someSignal");

        //Both processes should be on the final task execution
        allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        checkFinalStateForMultipleProcessesWithSimpleEventCatch(executionsByProcessInstance);

        //Complete the remaining tasks for both processes
        taskService.createTaskQuery().list().forEach(this::completeTask);
        assertProcessEnded(processInstance1.getId());
        assertProcessEnded(processInstance2.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateSignalCatchEvent.bpmn20.xml" })
    public void testSetCurrentExecutionToIntermediateCatchEventForMultipleProcessesTriggerSimultaneously() {
        ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");
        ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        List<Execution> allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        Map<String, List<Execution>> executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        checkInitialStateForMultipleProcessesWithSimpleEventCatch(executionsByProcessInstance);

        //Move both processes to the eventCatch
        ChangeActivityStateBuilder changeActivityStateBuilder = runtimeService.createChangeActivityStateBuilder();
        allExecutions.stream()
                .filter(e -> "beforeCatchEvent".equals(e.getActivityId()))
                .map(Execution::getId)
                .forEach(id -> changeActivityStateBuilder.moveExecutionToActivityId(id, "intermediateCatchEvent"));
        changeActivityStateBuilder.changeState();

        allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        checkWaitStateForMultipleProcessesWithSimpleEventCatch(executionsByProcessInstance);

        //Trigger signal
        runtimeService.signalEventReceived("someSignal");

        //Both processes should be on the final task execution
        allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        checkFinalStateForMultipleProcessesWithSimpleEventCatch(executionsByProcessInstance);

        //Complete the remaining tasks for both processes
        taskService.createTaskQuery().list().forEach(this::completeTask);
        assertProcessEnded(processInstance1.getId());
        assertProcessEnded(processInstance2.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateSignalCatchEvent.bpmn20.xml" })
    public void testSetCurrentActivityToIntermediateCatchEventForMultipleProcessesTriggerDiffered() {
        ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");
        ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        List<Execution> allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        Map<String, List<Execution>> executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        checkInitialStateForMultipleProcessesWithSimpleEventCatch(executionsByProcessInstance);

        //Move one process to the eventCatch
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance1.getId())
                .moveActivityIdTo("beforeCatchEvent", "intermediateCatchEvent")
                .changeState();

        allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        //ProcessInstance1 waiting for event
        String processId = processInstance1.getId();
        List<Execution> executions = executionsByProcessInstance.get(processId);
        Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processId).list();
        assertThat(tasks).isEmpty();



        //processInstance2 Execution still on initial state
        processId = processInstance2.getId();
        executions = executionsByProcessInstance.get(processId);
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).containsOnlyKeys("beforeCatchEvent");
        tasks = taskService.createTaskQuery().processInstanceId(processId).list();
        Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("beforeCatchEvent")).hasSize(1);


        //Trigger signal
        runtimeService.signalEventReceived("someSignal");

        //Move the second process to the eventCatch
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance2.getId())
                .moveActivityIdTo("beforeCatchEvent", "intermediateCatchEvent")
                .changeState();

        //ProcessInstance1 is on the postEvent task
        processId = processInstance1.getId();
        executions = runtimeService.createExecutionQuery().processInstanceId(processId).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("afterCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processId).list();
        classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("afterCatchEvent")).hasSize(1);


        //ProcessInstance2 is waiting for the event
        processId = processInstance2.getId();
        executions = runtimeService.createExecutionQuery().processInstanceId(processId).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processId).list();
        assertThat(tasks).isEmpty();


        //Fire the event once more
        runtimeService.signalEventReceived("someSignal");

        //Both process should be on the postEvent task execution
        allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        checkFinalStateForMultipleProcessesWithSimpleEventCatch(executionsByProcessInstance);

        //Complete the remaining tasks for both processes
        taskService.createTaskQuery().list().forEach(this::completeTask);
        assertProcessEnded(processInstance1.getId());
        assertProcessEnded(processInstance2.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/runtime/changestate/RuntimeServiceChangeStateTest.simpleIntermediateSignalCatchEvent.bpmn20.xml" })
    public void testSetCurrentExecutionToIntermediateCatchEventForMultipleProcessesTriggerDiffered() {
        ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");
        ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("changeStateForSimpleIntermediateEvent");

        List<Execution> allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        Map<String, List<Execution>> executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        checkInitialStateForMultipleProcessesWithSimpleEventCatch(executionsByProcessInstance);

        //Move one execution to the event catch
        String executionId = executionsByProcessInstance.get(processInstance1.getId()).stream()
                .filter(e -> "beforeCatchEvent".equals(e.getActivityId()))
                .findFirst()
                .map(Execution::getId)
                .get();
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(executionId, "intermediateCatchEvent")
                .changeState();

        allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        //ProcessInstance1 waiting for event
        String processId = processInstance1.getId();
        List<Execution> executions = executionsByProcessInstance.get(processId);
        Map<String, List<Execution>> classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processId).list();
        assertThat(tasks).isEmpty();


        //processInstance2 Execution still on initial state
        processId = processInstance2.getId();
        executions = executionsByProcessInstance.get(processId);
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).containsOnlyKeys("beforeCatchEvent");
        tasks = taskService.createTaskQuery().processInstanceId(processId).list();
        Map<String, List<Task>> classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("beforeCatchEvent")).hasSize(1);


        //Trigger signal
        runtimeService.signalEventReceived("someSignal");

        //Move the second process to the eventCatch
        executionId = executionsByProcessInstance.get(processInstance2.getId()).stream()
                .filter(e -> "beforeCatchEvent".equals(e.getActivityId()))
                .findFirst()
                .map(Execution::getId)
                .get();
        runtimeService.createChangeActivityStateBuilder()
                .moveExecutionToActivityId(executionId, "intermediateCatchEvent")
                .changeState();

        //ProcessInstance1 is on the postEvent task
        processId = processInstance1.getId();
        executions = runtimeService.createExecutionQuery().processInstanceId(processId).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("afterCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processId).list();
        classifiedTasks = groupListContentBy(tasks, Task::getTaskDefinitionKey);
        assertThat(classifiedTasks.get("afterCatchEvent")).hasSize(1);


        //ProcessInstance2 is waiting for the event
        processId = processInstance2.getId();
        executions = runtimeService.createExecutionQuery().processInstanceId(processId).onlyChildExecutions().list();
        classifiedExecutions = groupListContentBy(executions, Execution::getActivityId);
        assertThat(classifiedExecutions).hasSize(1);
        assertThat(classifiedExecutions.get("intermediateCatchEvent")).hasSize(1);
        tasks = taskService.createTaskQuery().processInstanceId(processId).list();
        assertThat(tasks).isEmpty();



        //Fire the event once more
        runtimeService.signalEventReceived("someSignal");

        //Both process should be on the postEvent task execution
        allExecutions = runtimeService.createExecutionQuery().onlyChildExecutions().list();
        executionsByProcessInstance = groupListContentBy(allExecutions, Execution::getProcessInstanceId);
        assertThat(executionsByProcessInstance).hasSize(2);

        checkFinalStateForMultipleProcessesWithSimpleEventCatch(executionsByProcessInstance);

        //Complete the remaining tasks for both processes
        taskService.createTaskQuery().list().forEach(this::completeTask);
        assertProcessEnded(processInstance1.getId());
        assertProcessEnded(processInstance2.getId());
    }


    protected String getJobActivityId(Job job) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jobConfigurationMap = objectMapper.readValue(job.getJobHandlerConfiguration(), new TypeReference<Map<String, Object>>() {

            });
            return (String) jobConfigurationMap.get("activityId");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    protected void completeTask(Task task) {
        taskService.complete(task.getId());
    }
    protected static<T> Map<String, List<T>> groupListContentBy(List<T> source, Function<T, String> classifier) {
        return source.stream().collect(Collectors.groupingBy(classifier));
    }
}

