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
package org.activiti.crystalball.simulator.impl.playback;

import org.activiti.crystalball.simulator.impl.EventRecorderTestUtils;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.startsWith;

/**
 * @author martin.grofcik
 */
public class PlaybackProcessStartTest extends AbstractPlaybackTest {
  public static final String SIMPLEST_PROCESS = "theSimplestProcess";
  public static final String BUSINESS_KEY = "testBusinessKey";
  public static final String TEST_VALUE = "TestValue";
  public static final String TEST_VARIABLE = "testVariable";

  @CheckStatus(methodName = "demoCheckStatus")
  @Deployment
  public void testDemo() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(TEST_VARIABLE, TEST_VALUE);
    processEngine.getRuntimeService().startProcessInstanceByKey(SIMPLEST_PROCESS, BUSINESS_KEY, variables);
  }

  
  public void demoCheckStatus() {
    final HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().
      finished().
      includeProcessVariables().
      singleResult();
    assertNotNull(historicProcessInstance);
    RepositoryService repositoryService = processEngine.getRepositoryService();
    final ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
      processDefinitionId(historicProcessInstance.getProcessDefinitionId()).
      singleResult();
    assertEquals(SIMPLEST_PROCESS, processDefinition.getKey());

    assertEquals(1, historicProcessInstance.getProcessVariables().size());
    assertEquals(TEST_VALUE, historicProcessInstance.getProcessVariables().get(TEST_VARIABLE));
    assertEquals(BUSINESS_KEY, historicProcessInstance.getBusinessKey());
  }

  @CheckStatus(methodName = "messageProcessStartCheckStatus")
  @Deployment
  public void testMessageProcessStart() {

    runtimeService.startProcessInstanceByMessage("startProcessMessage");
  }

  
  public void messageProcessStartCheckStatus() {
    final HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().
      finished().
      singleResult();
    assertNotNull(historicProcessInstance);
    assertTrue(startsWith(historicProcessInstance.getProcessDefinitionId(), "messageStartEventProcess"));
  }

  @Deployment
  @CheckStatus(methodName = "checkStatus")
  public void testSignals() throws Exception {
    runtimeService.startProcessInstanceByKey("catchSignal");
    EventRecorderTestUtils.increaseTime(this.processEngineConfiguration.getClock());
    runtimeService.startProcessInstanceByKey("throwSignal");
  }

  
  public void checkStatus() {
    final List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().
                                                            finished().
                                                            list();
    assertNotNull(historicProcessInstances);
    assertEquals(2, historicProcessInstances.size());
  }

  @Deployment
  @CheckStatus(methodName = "userTaskCheckStatus")
  public void testUserTask() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "oneTaskProcessBusinessKey");
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask").singleResult();
    EventRecorderTestUtils.increaseTime(processEngineConfiguration.getClock());
    taskService.complete(task.getId());
  }

  
  public void userTaskCheckStatus() {
    final HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().
      finished().
      singleResult();
    assertNotNull(historicProcessInstance);
    assertEquals("oneTaskProcessBusinessKey", historicProcessInstance.getBusinessKey());
    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("userTask").singleResult();
    assertEquals("user1", historicTaskInstance.getAssignee());
  }
}