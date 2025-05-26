/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.test.api.history;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.Set;

public class NonCascadeDeleteTest extends PluggableActivitiTestCase {

  private static String PROCESS_DEFINITION_KEY = "oneTaskProcess";

  private String deploymentId;

  private String processInstanceId;

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
	  super.tearDown();
  }
  @Test
  public void testHistoricProcessInstanceQuery(){
    deploymentId = repositoryService.createDeployment()
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
      .deploy().getId();

    processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    taskService.complete(task.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        assertThat(processInstance.getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);

        // Delete deployment and historic process instance remains.
        repositoryService.deleteDeployment(deploymentId, false);

        // Historic process instance remains (by querying processInstance)
        HistoricProcessInstance processInstanceAfterDelete = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        assertThat(processInstanceAfterDelete.getProcessDefinitionKey()).isNull();
        assertThat(processInstanceAfterDelete.getProcessDefinitionName()).isNull();
        assertThat(processInstanceAfterDelete.getProcessDefinitionVersion()).isNull();

        // Historic process instance remains (by querying processInstances)
        processInstanceAfterDelete = historyService.createHistoricProcessInstanceQuery().processInstanceIds(Set.of(processInstanceId)).singleResult();
        assertThat(processInstanceAfterDelete.getProcessDefinitionKey()).isNull();
        assertThat(processInstanceAfterDelete.getProcessDefinitionName()).isNull();
        assertThat(processInstanceAfterDelete.getProcessDefinitionVersion()).isNull();

        // clean
        historyService.deleteHistoricProcessInstance(processInstanceId);
    }
  }
}
