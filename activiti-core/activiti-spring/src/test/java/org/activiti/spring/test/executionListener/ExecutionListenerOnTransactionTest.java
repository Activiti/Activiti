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
package org.activiti.spring.test.executionListener;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

/**

 */
@ContextConfiguration("classpath:org/activiti/spring/test/executionListener/TransactionDependentListenerTest-context.xml")
public class ExecutionListenerOnTransactionTest extends SpringActivitiTestCase {

    private void cleanUp() {
        List<org.activiti.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.activiti.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(),
                                               true);
        }
    }

    @Override
    public void tearDown() {
        cleanUp();
    }

    @Deployment
    public void testOnClosedExecutionListenersWithRollback() {

        CurrentActivityTransactionDependentExecutionListener.clear();

        Map<String, Object> variables = new HashMap<>();
        variables.put("serviceTask1",
                      false);
        variables.put("serviceTask2",
                      false);
        variables.put("serviceTask3",
                      true);

        processEngineConfiguration.setAsyncExecutorActivate(false);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionDependentExecutionListenerProcess",
                                                                                   variables);

        // execute the only job that should be there 1 time
        try {
            managementService.executeJob(managementService.createJobQuery().singleResult().getId());
        } catch (Exception ex) {
            // expected; serviceTask3 throws exception
        }

        List<CurrentActivityTransactionDependentExecutionListener.CurrentActivity> currentActivities = CurrentActivityTransactionDependentExecutionListener.getCurrentActivities();
        assertThat(currentActivities).hasSize(1);

        assertThat(currentActivities.get(0).getActivityId()).isEqualTo("serviceTask1");
        assertThat(currentActivities.get(0).getActivityName()).isEqualTo("Service Task 1");
        assertThat(currentActivities.get(0).getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(currentActivities.get(0).getProcessInstanceId()).isNotNull();

        assertThat(managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
        assertThat(activeActivityIds).hasSize(1);
        assertThat(activeActivityIds.get(0)).isEqualTo("serviceTask2");
    }

    @Deployment
    public void testOnCloseFailureExecutionListenersWithRollback() {

        CurrentActivityTransactionDependentExecutionListener.clear();

        Map<String, Object> variables = new HashMap<>();
        variables.put("serviceTask1",
                      false);
        variables.put("serviceTask2",
                      false);
        variables.put("serviceTask3",
                      true);

        processEngineConfiguration.setAsyncExecutorActivate(false);

        runtimeService.startProcessInstanceByKey("transactionDependentExecutionListenerProcess",
                                                 variables);

        // execute the only job that should be there 1 time
        try {
            managementService.executeJob(managementService.createJobQuery().singleResult().getId());
        } catch (Exception ex) {
            // expected; serviceTask3 throws exception
        }

        List<CurrentActivityTransactionDependentExecutionListener.CurrentActivity> currentActivities = CurrentActivityTransactionDependentExecutionListener.getCurrentActivities();
        assertThat(currentActivities).hasSize(2);

        // the before commit listener
        assertThat(currentActivities.get(0).getActivityId()).isEqualTo("serviceTask1");
        assertThat(currentActivities.get(0).getActivityName()).isEqualTo("Service Task 1");

        // the before rolled-back listener
        assertThat(currentActivities.get(1).getActivityId()).isEqualTo("serviceTask3");
        assertThat(currentActivities.get(1).getActivityName()).isEqualTo("Service Task 3");
    }

    @Deployment
    public void testOnClosedExecutionListenersWithExecutionVariables() {

        CurrentActivityTransactionDependentExecutionListener.clear();

        runtimeService.startProcessInstanceByKey("transactionDependentExecutionListenerProcess");

        List<CurrentActivityTransactionDependentExecutionListener.CurrentActivity> currentActivities = CurrentActivityTransactionDependentExecutionListener.getCurrentActivities();
        assertEquals(3,
                     currentActivities.size());

        assertEquals("serviceTask1",
                     currentActivities.get(0).getActivityId());
        assertEquals("Service Task 1",
                     currentActivities.get(0).getActivityName());
        assertEquals(0,
                     currentActivities.get(0).getExecutionVariables().size());

        assertEquals("serviceTask2",
                     currentActivities.get(1).getActivityId());
        assertEquals("Service Task 2",
                     currentActivities.get(1).getActivityName());
        assertEquals(1,
                     currentActivities.get(1).getExecutionVariables().size());
        assertEquals("test1",
                     currentActivities.get(1).getExecutionVariables().get("injectedExecutionVariable"));

        assertEquals("serviceTask3",
                     currentActivities.get(2).getActivityId());
        assertEquals("Service Task 3",
                     currentActivities.get(2).getActivityName());
        assertEquals(1,
                     currentActivities.get(2).getExecutionVariables().size());
        assertEquals("test2",
                     currentActivities.get(2).getExecutionVariables().get("injectedExecutionVariable"));
    }

    @Deployment
    public void testOnCloseFailureExecutionListenersWithTransactionalOperation() throws InterruptedException {

        MyTransactionalOperationTransactionDependentExecutionListener.clear();

        ProcessInstance firstProcessInstance = runtimeService.startProcessInstanceByKey("transactionDependentExecutionListenerProcess");
        assertProcessEnded(firstProcessInstance.getId());

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
            assertEquals(1,
                         historicProcessInstances.size());
            assertEquals("transactionDependentExecutionListenerProcess",
                         historicProcessInstances.get(0).getProcessDefinitionKey());
        }

        Thread.sleep(3);
        ProcessInstance secondProcessInstance = runtimeService.startProcessInstanceByKey("secondTransactionDependentExecutionListenerProcess");
        assertProcessEnded(secondProcessInstance.getId());

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            // first historic process instance was deleted by execution listener
            List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
            assertEquals(1,
                         historicProcessInstances.size());
            assertEquals("secondTransactionDependentExecutionListenerProcess",
                         historicProcessInstances.get(0).getProcessDefinitionKey());
        }

        List<MyTransactionalOperationTransactionDependentExecutionListener.CurrentActivity> currentActivities = MyTransactionalOperationTransactionDependentExecutionListener.getCurrentActivities();
        assertEquals(1,
                     currentActivities.size());

        assertEquals("serviceTask1",
                     currentActivities.get(0).getActivityId());
        assertEquals("Service Task 1",
                     currentActivities.get(0).getActivityName());
    }

    @Deployment
    public void testOnClosedExecutionListenersWithCustomPropertiesResolver() {

        MyTransactionalOperationTransactionDependentExecutionListener.clear();

        runtimeService.startProcessInstanceByKey("transactionDependentExecutionListenerProcess");

        List<CurrentActivityTransactionDependentExecutionListener.CurrentActivity> currentActivities = CurrentActivityTransactionDependentExecutionListener.getCurrentActivities();
        assertEquals(1,
                     currentActivities.size());

        assertEquals("serviceTask1",
                     currentActivities.get(0).getActivityId());
        assertEquals("Service Task 1",
                     currentActivities.get(0).getActivityName());
        assertEquals(1,
                     currentActivities.get(0).getCustomPropertiesMap().size());
        assertEquals("serviceTask1",
                     currentActivities.get(0).getCustomPropertiesMap().get("customProp1"));
    }
}
