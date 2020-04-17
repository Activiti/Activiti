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

package org.activiti.engine.test.bpmn.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Date;

import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class EventBasedGatewayTest extends PluggableActivitiTestCase {

    @Deployment(resources = {"org/activiti/engine/test/bpmn/gateway/EventBasedGatewayTest.testCatchAlertAndTimer.bpmn20.xml",
            "org/activiti/engine/test/bpmn/gateway/EventBasedGatewayTest.throwAlertSignal.bpmn20.xml"})
    public void testCatchSignalCancelsTimer() {

        ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("catchSignal");

        assertThat(createEventSubscriptionQuery().count()).isEqualTo(1);
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);

        ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("throwSignal");

        assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
        assertThat(managementService.createJobQuery().count()).isEqualTo(0);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);

        Task task = taskService.createTaskQuery().taskName("afterSignal").singleResult();
        assertThat(task).isNotNull();
        taskService.complete(task.getId());

        assertHistoricActivitiesDeleteReason(pi1,
                                             DeleteReason.EVENT_BASED_GATEWAY_CANCEL,
                                             "timerEvent");
    }

    @Deployment(resources = {"org/activiti/engine/test/bpmn/gateway/EventBasedGatewayTest.testCatchAlertAndTimer.bpmn20.xml"})
    public void testCatchTimerCancelsSignal() {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("catchSignal");

        assertThat(createEventSubscriptionQuery().count()).isEqualTo(1);
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);

        processEngineConfiguration.getClock().setCurrentTime(new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() + 10000));

        // wait for timer to fire
        waitForJobExecutorToProcessAllJobs(10000,
                                           100);

        assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
        assertThat(managementService.createJobQuery().count()).isEqualTo(0);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);

        Task task = taskService.createTaskQuery().taskName("afterTimer").singleResult();

        assertThat(task).isNotNull();

        taskService.complete(task.getId());

        assertHistoricActivitiesDeleteReason(processInstance,
                                             DeleteReason.EVENT_BASED_GATEWAY_CANCEL,
                                             "signalEvent");
    }

    @Deployment
    public void testCatchSignalAndMessageAndTimer() {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("catchSignal");

        assertThat(createEventSubscriptionQuery().count()).isEqualTo(2);
        EventSubscriptionQueryImpl messageEventSubscriptionQuery = createEventSubscriptionQuery().eventType("message");
        assertThat(messageEventSubscriptionQuery.count()).isEqualTo(1);
        assertThat(createEventSubscriptionQuery().eventType("signal").count()).isEqualTo(1);
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);

        Execution execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("newInvoice").singleResult();
        assertThat(execution).isNotNull();

        execution = runtimeService.createExecutionQuery().signalEventSubscriptionName("alert").singleResult();
        assertThat(execution).isNotNull();

        processEngineConfiguration.getClock().setCurrentTime(new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() + 10000));

        EventSubscriptionEntity messageEventSubscription = messageEventSubscriptionQuery.singleResult();
        runtimeService.messageEventReceived(messageEventSubscription.getEventName(),
                                            messageEventSubscription.getExecutionId());

        assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);
        assertThat(managementService.createJobQuery().count()).isEqualTo(0);

        Task task = taskService.createTaskQuery().taskName("afterMessage").singleResult();
        assertThat(task).isNotNull();
        taskService.complete(task.getId());

        assertHistoricActivitiesDeleteReason(processInstance,
                                             DeleteReason.EVENT_BASED_GATEWAY_CANCEL,
                                             "signalEvent");
        assertHistoricActivitiesDeleteReason(processInstance,
                                             DeleteReason.EVENT_BASED_GATEWAY_CANCEL,
                                             "timerEvent");
    }

    public void testConnectedToActivity() {
        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/gateway/EventBasedGatewayTest.testConnectedToActivity.bpmn20.xml").deploy())
            .withMessageContaining("Event based gateway can only be connected to elements of type intermediateCatchEvent");
    }

    @Deployment
    public void testAsyncEventBasedGateway() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncEventBasedGateway");

        // Trying to fire the signal should fail, job not yet created
        runtimeService.signalEventReceived("alert");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task).isNull();

        Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(job).isNotNull();

        managementService.executeJob(job.getId());
        runtimeService.signalEventReceived("alert");
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getName()).isEqualTo("afterSignal");
    }

    private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
        return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
    }
}
