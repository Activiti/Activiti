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

    executeJobExecutorForTime(14000, 500);

    List<CurrentActivityTransactionDependentExecutionListener.CurrentActivity> currentActivities = CurrentActivityTransactionDependentExecutionListener.getCurrentActivities();
    assertEquals(2, currentActivities.size());

    assertEquals("theStart", currentActivities.get(0).getActivityId());
    assertEquals("Start Event", currentActivities.get(0).getActivityName());

    assertEquals("serviceTask1", currentActivities.get(1).getActivityId());
    assertEquals("Service Task 1", currentActivities.get(1).getActivityName());
  }

  @Deployment
  public void testOnCloseFailureExecutionListenersWithRollback() {

    CurrentActivityTransactionDependentExecutionListener.clear();

    Map<String, Object> variables = new HashMap<>();
    variables.put("serviceTask1", false);
    variables.put("serviceTask2", false);
    variables.put("serviceTask3", true);

    processEngineConfiguration.setJobExecutorActivate(false);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionDependentExecutionListenerProcess", variables);

    executeJobExecutorForTime(14000, 500);

    List<CurrentActivityTransactionDependentExecutionListener.CurrentActivity> currentActivities = CurrentActivityTransactionDependentExecutionListener.getCurrentActivities();
    assertEquals(8, currentActivities.size());

    assertEquals("theStart", currentActivities.get(0).getActivityId());
    assertEquals("Start Event", currentActivities.get(0).getActivityName());

    assertEquals("serviceTask1", currentActivities.get(1).getActivityId());
    assertEquals("Service Task 1", currentActivities.get(1).getActivityName());

    assertEquals("serviceTask2", currentActivities.get(2).getActivityId());
    assertEquals("Service Task 2", currentActivities.get(2).getActivityName());

    assertEquals("serviceTask3", currentActivities.get(3).getActivityId());
    assertEquals("Service Task 3", currentActivities.get(3).getActivityName());

    assertEquals("serviceTask2", currentActivities.get(4).getActivityId());
    assertEquals("Service Task 2", currentActivities.get(4).getActivityName());

    assertEquals("serviceTask3", currentActivities.get(5).getActivityId());
    assertEquals("Service Task 3", currentActivities.get(5).getActivityName());

    assertEquals("serviceTask2", currentActivities.get(6).getActivityId());
    assertEquals("Service Task 2", currentActivities.get(6).getActivityName());

    assertEquals("serviceTask3", currentActivities.get(7).getActivityId());
    assertEquals("Service Task 3", currentActivities.get(7).getActivityName());
  }

}
