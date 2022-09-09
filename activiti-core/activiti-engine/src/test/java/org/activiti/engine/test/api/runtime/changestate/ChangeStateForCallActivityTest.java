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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
    @author LoveMyOrange
 */
public class ChangeStateForCallActivityTest extends PluggableActivitiTestCase {

    private ChangeStateEventListener changeStateEventListener = new ChangeStateEventListener();

    @Override
    protected void setUp() {
        processEngine.getRuntimeService().addEventListener(changeStateEventListener);
    }

    @Override
    protected void tearDown() {
        processEngine.getRuntimeService().removeEventListener(changeStateEventListener);
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksParentProcess.bpmn20.xml", "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
    public void testSetCurrentActivityInParentProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksParentProcess");


        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

        taskService.complete(task.getId());


        ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
        assertThat(subProcessInstance).isNotNull();

        task = taskService.createTaskQuery().processInstanceId(subProcessInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask");

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(subProcessInstance.getId())
                .moveActivityIdToParentActivityId("theTask", "secondTask")
                .changeState();


        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");

        assertThat(runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).count()).isZero();
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(subProcessInstance.getId()).count()).isZero();

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions).hasSize(1);

        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksParentProcessV2.bpmn20.xml", "org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml" })
    public void testSetCurrentActivityInParentProcessV2() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksParentProcess");



        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");
        taskService.complete(task.getId());



        ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
        assertThat(subProcessInstance).isNotNull();

        task = taskService.createTaskQuery().processInstanceId(subProcessInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");
        taskService.complete(task.getId());



        task = taskService.createTaskQuery().processInstanceId(subProcessInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(subProcessInstance.getId())
                .moveActivityIdToParentActivityId("secondTask", "secondTask")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
            assertThat(historicTaskInstance.getTaskDefinitionKey()).isEqualTo("secondTask");
        }

        assertThat(runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).count()).isZero();
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(subProcessInstance.getId()).count()).isZero();

        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().list();
        assertThat(executions).hasSize(1);

        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksParentProcess.bpmn20.xml", "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
    public void testSetCurrentActivityInSubProcessInstance() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksParentProcess");


        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdToSubProcessInstanceActivityId("firstTask", "theTask", "callActivity")
                .changeState();


        ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
        assertThat(subProcessInstance).isNotNull();

        assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).count()).isZero();
        assertThat(taskService.createTaskQuery().processInstanceId(subProcessInstance.getId()).count()).isEqualTo(1);

        assertThat(runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count()).isEqualTo(1);
        assertThat(runtimeService.createExecutionQuery().processInstanceId(subProcessInstance.getId()).onlyChildExecutions().count()).isEqualTo(1);

        task = taskService.createTaskQuery().processInstanceId(subProcessInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask");
        taskService.complete(task.getId());


        assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(subProcessInstance.getId()).count()).isZero();

        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");

        taskService.complete(task.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksParentProcessV2.bpmn20.xml", "org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml" })
    public void testSetCurrentActivityInSubProcessInstanceV2() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksParentProcess");


        Task firstTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(firstTask.getTaskDefinitionKey()).isEqualTo("firstTask");



        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdToSubProcessInstanceActivityId("firstTask", "secondTask", "callActivity")
                .changeState();

        ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
        assertThat(subProcessInstance).isNotNull();

        assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).count()).isZero();
        assertThat(taskService.createTaskQuery().processInstanceId(subProcessInstance.getId()).count()).isEqualTo(1);

        assertThat(runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count()).isEqualTo(1);
        assertThat(runtimeService.createExecutionQuery().processInstanceId(subProcessInstance.getId()).onlyChildExecutions().count()).isEqualTo(1);

        Task firstSecondTask = taskService.createTaskQuery().processInstanceId(subProcessInstance.getId()).singleResult();
        assertThat(firstSecondTask.getTaskDefinitionKey()).isEqualTo("secondTask");

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(firstSecondTask.getId()).singleResult();
            assertThat(historicTaskInstance.getTaskDefinitionKey()).isEqualTo("secondTask");
        }



        taskService.complete(firstSecondTask.getId());


        assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(subProcessInstance.getId()).count()).isZero();

        Task secondTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(secondTask.getTaskDefinitionKey()).isEqualTo("secondTask");

        taskService.complete(secondTask.getId());

        assertProcessEnded(processInstance.getId());
    }


    @Deployment(resources = { "org/activiti/engine/test/api/variables/callActivityWithCalledElementExpression.bpmn20.xml" })
    public void testSetCurrentActivityInSubProcessInstanceWithCalledElementExpression() {
        try {
            //Deploy second version of the process definition
            deployProcessDefinition("my deploy", "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml");

            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("calledElementExpression");



            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

            //First change state attempt fails as the calledElement expression cannot be evaluated
            assertThatThrownBy(() -> runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(processInstance.getId())
                    .moveActivityIdToSubProcessInstanceActivityId("firstTask", "theTask", "callActivity")
                    .changeState())
                    .isExactlyInstanceOf(ActivitiException.class)
                    .hasMessage("Cannot resolve calledElement expression '${subProcessDefId}' of callActivity 'callActivity'");

            //Change state specifying the variable with the value
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(processInstance.getId())
                    .moveActivityIdToSubProcessInstanceActivityId("firstTask", "theTask", "callActivity", 1)
                    .processVariable("subProcessDefId", "oneTaskProcess")
                    .changeState();


            ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
            assertThat(subProcessInstance).isNotNull();

            assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).count()).isZero();
            assertThat(taskService.createTaskQuery().processInstanceId(subProcessInstance.getId()).count()).isEqualTo(1);

            assertThat(runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count()).isEqualTo(1);
            assertThat(runtimeService.createExecutionQuery().processInstanceId(subProcessInstance.getId()).onlyChildExecutions().count()).isEqualTo(1);

            task = taskService.createTaskQuery().processInstanceId(subProcessInstance.getId()).singleResult();
            assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask");
            taskService.complete(task.getId());


            assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(subProcessInstance.getId()).count()).isZero();

            task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            assertThat(task.getTaskDefinitionKey()).isEqualTo("lastTask");

            taskService.complete(task.getId());

            assertProcessEnded(processInstance.getId());

        } finally {
            deleteDeployments();
        }
    }


    @Deployment(resources = { "org/activiti/engine/test/api/twoTasksParentProcess.bpmn20.xml", "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
    public void testSetCurrentActivityInSubProcessInstanceSpecificVersion() {
        try {
            // Deploy second version of the process definition
            ProcessDefinition processDefinition = deployProcessDefinition("my deploy", "org/activiti/engine/test/api/oneTaskProcessV2.bpmn20.xml");

            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksParentProcess");



            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            assertThat(task.getTaskDefinitionKey()).isEqualTo("firstTask");

            assertThatThrownBy(() -> runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(processInstance.getId())
                    .moveActivityIdToSubProcessInstanceActivityId("firstTask", "theTask", "callActivity")
                    .changeState())
                    .isExactlyInstanceOf(ActivitiException.class)
                    .hasMessage("Cannot find activity 'theTask' in process definition with id 'oneTaskProcess'");

            //Invalid "unExistent" process definition version
            assertThatThrownBy(() -> runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(processInstance.getId())
                    .moveActivityIdToSubProcessInstanceActivityId("firstTask", "theTask", "callActivity", 5)
                    .changeState())
                    .isExactlyInstanceOf(ActivitiException.class)
                    .hasMessage("Cannot find activity 'theTask' in process definition with id 'oneTaskProcess'");

            //Change state specifying the first version
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(processInstance.getId())
                    .moveActivityIdToSubProcessInstanceActivityId("firstTask", "theTask", "callActivity", 1)
                    .changeState();



            ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
            assertThat(subProcessInstance).isNotNull();

            assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).count()).isZero();
            assertThat(taskService.createTaskQuery().processInstanceId(subProcessInstance.getId()).count()).isEqualTo(1);

            assertThat(runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().count()).isEqualTo(1);
            assertThat(runtimeService.createExecutionQuery().processInstanceId(subProcessInstance.getId()).onlyChildExecutions().count()).isEqualTo(1);

            task = taskService.createTaskQuery().processInstanceId(subProcessInstance.getId()).singleResult();
            assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask");
            taskService.complete(task.getId());


            assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(subProcessInstance.getId()).count()).isZero();

            task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            assertThat(task.getTaskDefinitionKey()).isEqualTo("secondTask");

            taskService.complete(task.getId());

            assertProcessEnded(processInstance.getId());

        } finally {
            deleteDeployments();
        }
    }

    protected ProcessDefinition deployProcessDefinition(String name, String path) {
        org.activiti.engine.repository.Deployment deployment = repositoryService.createDeployment()
            .name(name)
            .addClasspathResource(path)
            .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deployment.getId()).singleResult();

        return processDefinition;
    }

    protected void deleteDeployments() {
        for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }
}
