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
package org.activiti.standalone.escapeclause;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

public class HistoricVariableInstanceEscapeClauseTest extends AbstractEscapeClauseTestCase {

  private String deploymentOneId;

  private String deploymentTwoId;

  private ProcessInstance processInstance1;

  private ProcessInstance processInstance2;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
      .createDeployment()
      .tenantId("One%")
      .addClasspathResource("org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .tenantId("Two_")
      .addClasspathResource("org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
      .deploy()
      .getId();

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var%", "One%");
    processInstance1 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", vars, "One%");
    runtimeService.setProcessInstanceName(processInstance1.getId(), "One%");

    vars = new HashMap<String, Object>();
    vars.put("var_", "Two_");
    processInstance2 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", vars, "Two_");
    runtimeService.setProcessInstanceName(processInstance2.getId(), "Two_");

    Task task = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    taskService.complete(task.getId());

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }

  @Test
  public void testQueryByVariableNameLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableNameLike("%\\%%").singleResult();
        assertThat(historicVariable).isNotNull();
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance1.getId());
        assertThat(historicVariable.getValue()).isEqualTo("One%");

        historicVariable = historyService.createHistoricVariableInstanceQuery().variableNameLike("%\\_%").singleResult();
        assertThat(historicVariable).isNotNull();
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance2.getId());
        assertThat(historicVariable.getValue()).isEqualTo("Two_");
    }
  }

  @Test
  public void testQueryLikeByQueryVariableValue() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLike("var%", "%\\%%").singleResult();
        assertThat(historicVariable).isNotNull();
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance1.getId());

        historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLike("var_", "%\\_%").singleResult();
        assertThat(historicVariable).isNotNull();
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance2.getId());
    }
  }

  @Test
  public void testQueryLikeByQueryVariableValueIgnoreCase() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLikeIgnoreCase("var%", "%\\%%").singleResult();
        assertThat(historicVariable).isNotNull();
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance1.getId());

        historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLikeIgnoreCase("var_", "%\\_%").singleResult();
        assertThat(historicVariable).isNotNull();
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance2.getId());
    }
  }
}
