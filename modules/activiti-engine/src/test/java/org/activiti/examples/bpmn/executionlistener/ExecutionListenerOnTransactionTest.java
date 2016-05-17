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
package org.activiti.examples.bpmn.executionlistener;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Yvo Swillens
 */
public class ExecutionListenerOnTransactionTest extends PluggableActivitiTestCase {

  @Deployment
  public void testOnClosedExecutionListenersWithRollback() {

    CurrentActivityTransactionDependentExecutionListener.clear();

    Map<String, Object> variables = new HashMap<>();
    variables.put("serviceTask1", false);
    variables.put("serviceTask2", false);
    variables.put("serviceTask3", true);

    processEngineConfiguration.setJobExecutorActivate(false);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionDependentExecutionListenerProcess", variables);

    // execute the only job that should be there 1 time
    try {
      managementService.executeJob(managementService.createJobQuery().singleResult().getId());

    } catch (Exception ex) {
      // expected; serviceTask3 throws exception
    }

    List<CurrentActivityTransactionDependentExecutionListener.CurrentActivity> currentActivities = CurrentActivityTransactionDependentExecutionListener.getCurrentActivities();
    assertEquals(1, currentActivities.size());

    assertEquals("serviceTask1", currentActivities.get(0).getActivityId());
    assertEquals("Service Task 1", currentActivities.get(0).getActivityName());
    assertEquals(processInstance.getId(), currentActivities.get(0).getProcessInstanceId());
    assertNotNull(currentActivities.get(0).getProcessInstanceId());
  }

  @Deployment
  public void testOnCloseFailureExecutionListenersWithRollback() {

    CurrentActivityTransactionDependentExecutionListener.clear();

    Map<String, Object> variables = new HashMap<>();
    variables.put("serviceTask1", false);
    variables.put("serviceTask2", false);
    variables.put("serviceTask3", true);

    processEngineConfiguration.setJobExecutorActivate(false);

    runtimeService.startProcessInstanceByKey("transactionDependentExecutionListenerProcess", variables);

    // execute the only job that should be there 1 time
    try {
      managementService.executeJob(managementService.createJobQuery().singleResult().getId());
    } catch (Exception ex) {
      // expected; serviceTask3 throws exception
    }

    List<CurrentActivityTransactionDependentExecutionListener.CurrentActivity> currentActivities = CurrentActivityTransactionDependentExecutionListener.getCurrentActivities();
    assertEquals(3, currentActivities.size());

    assertEquals("serviceTask1", currentActivities.get(0).getActivityId());
    assertEquals("Service Task 1", currentActivities.get(0).getActivityName());

    assertEquals("serviceTask2", currentActivities.get(1).getActivityId());
    assertEquals("Service Task 2", currentActivities.get(1).getActivityName());

    assertEquals("serviceTask3", currentActivities.get(2).getActivityId());
    assertEquals("Service Task 3", currentActivities.get(2).getActivityName());
  }

  @Deployment
  public void testOnClosedExecutionListenersWithExecutionVariables() {

    CurrentActivityTransactionDependentExecutionListener.clear();

    runtimeService.startProcessInstanceByKey("transactionDependentExecutionListenerProcess");

    List<CurrentActivityTransactionDependentExecutionListener.CurrentActivity> currentActivities = CurrentActivityTransactionDependentExecutionListener.getCurrentActivities();
    assertEquals(3, currentActivities.size());

    assertEquals("serviceTask1", currentActivities.get(0).getActivityId());
    assertEquals("Service Task 1", currentActivities.get(0).getActivityName());
    assertEquals(0, currentActivities.get(0).getExecutionVariables().size());

    assertEquals("serviceTask2", currentActivities.get(1).getActivityId());
    assertEquals("Service Task 2", currentActivities.get(1).getActivityName());
    assertEquals(1, currentActivities.get(1).getExecutionVariables().size());
    assertEquals("test1", currentActivities.get(1).getExecutionVariables().get("injectedExecutionVariable"));

    assertEquals("serviceTask3", currentActivities.get(2).getActivityId());
    assertEquals("Service Task 3", currentActivities.get(2).getActivityName());
    assertEquals(1, currentActivities.get(2).getExecutionVariables().size());
    assertEquals("test2", currentActivities.get(2).getExecutionVariables().get("injectedExecutionVariable"));
  }

  @Deployment
  public void testOnCloseFailureExecutionListenersWithTransactionalOperation() {

    TransactionalOperationTransactionDependentExecutionListener.clear();

    ProcessInstance firstProcessInstance = runtimeService.startProcessInstanceByKey("transactionDependentExecutionListenerProcess");
    assertProcessEnded(firstProcessInstance.getId());

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
    assertEquals(1, historicProcessInstances.size());
    assertEquals("transactionDependentExecutionListenerProcess", historicProcessInstances.get(0).getProcessDefinitionKey());

    ProcessInstance secondProcessInstance = runtimeService.startProcessInstanceByKey("secondTransactionDependentExecutionListenerProcess");
    assertProcessEnded(secondProcessInstance.getId());

    // first historic process instance was deleted by execution listener
    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
    assertEquals(1, historicProcessInstances.size());
    assertEquals("secondTransactionDependentExecutionListenerProcess", historicProcessInstances.get(0).getProcessDefinitionKey());

    List<TransactionalOperationTransactionDependentExecutionListener.CurrentActivity> currentActivities = TransactionalOperationTransactionDependentExecutionListener.getCurrentActivities();
    assertEquals(1, currentActivities.size());

    assertEquals("serviceTask1", currentActivities.get(0).getActivityId());
    assertEquals("Service Task 1", currentActivities.get(0).getActivityName());
  }

  @Deployment
  public void testOnClosedExecutionListenersWithCustomPropertiesResolver() {

    TransactionalOperationTransactionDependentExecutionListener.clear();

    runtimeService.startProcessInstanceByKey("transactionDependentExecutionListenerProcess");

    List<CurrentActivityTransactionDependentExecutionListener.CurrentActivity> currentActivities = CurrentActivityTransactionDependentExecutionListener.getCurrentActivities();
    assertEquals(1, currentActivities.size());

    assertEquals("serviceTask1", currentActivities.get(0).getActivityId());
    assertEquals("Service Task 1", currentActivities.get(0).getActivityName());
    assertEquals(2, currentActivities.get(0).getCustomPropertiesMap().size());
    assertEquals("test one", currentActivities.get(0).getCustomPropertiesMap().get("customProp1"));
    assertEquals("test two", currentActivities.get(0).getCustomPropertiesMap().get("customProp2"));
  }

}
