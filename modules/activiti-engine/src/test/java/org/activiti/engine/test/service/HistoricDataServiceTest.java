/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.service;

import java.util.List;

import org.activiti.engine.ProcessInstance;
import org.activiti.engine.Task;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.ProcessEngineImplTestCase;

/**
 * @author Christian Stettler
 */
public class HistoricDataServiceTest extends ProcessEngineImplTestCase {

  @Deployment(resources = {"oneTaskProcess.bpmn20.xml"})
  public void testHistoricDataCreatedForProcessExecution() {
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    try {
      HistoricProcessInstance historicProcessInstance = historicDataService.findHistoricProcessInstance(processInstance.getId());

      assertNotNull(historicProcessInstance);
      assertEquals(processInstance.getId(), historicProcessInstance.getProcessInstanceId());
      assertEquals(processInstance.getProcessDefinitionId(), historicProcessInstance.getProcessDefinitionId());
      assertNotNull(historicProcessInstance.getStartTime());
      assertNull(historicProcessInstance.getEndTime());
      assertNull(historicProcessInstance.getDurationInMillis());

      // TODO: check for HistoricActivityInstance created once events get fired

      List<Task> tasks = taskService.createTaskQuery().processInstance(processInstance.getId()).list();

      assertEquals(1, tasks.size());

      taskService.complete(tasks.get(0).getId());

      historicProcessInstance = historicDataService.findHistoricProcessInstance(processInstance.getId());

      assertNotNull(historicProcessInstance);
      assertEquals(processInstance.getId(), historicProcessInstance.getProcessInstanceId());
      assertEquals(processInstance.getProcessDefinitionId(), historicProcessInstance.getProcessDefinitionId());
      assertNotNull(historicProcessInstance.getStartTime());
      assertNotNull(historicProcessInstance.getEndTime());
      assertNotNull(historicProcessInstance.getDurationInMillis());

      // TODO: check for HistoricActivityInstance updated once events get fired
    } finally {
      processEngineConfiguration.getCommandExecutor().execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          commandContext.getHistorySession().deleteHistoricProcessInstance(processInstance.getId());
          return null;
        }
      });
    }
  }
}
