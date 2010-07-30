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

package org.activiti.test.service;

import static org.junit.Assert.*;

import java.util.List;

import org.activiti.engine.ProcessInstance;
import org.activiti.engine.Task;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christian Stettler
 */
public class HistoricDataServiceTest {

  @Rule
  public LogInitializer logSetup = new LogInitializer();

  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  @Deployment(resources = {"oneTaskProcess.bpmn20.xml"})
  public void testHistoricDataCreatedForProcessExecution() {
    final ProcessInstance processInstance = deployer.getProcessService().startProcessInstanceByKey("oneTaskProcess");

    try {
      HistoricProcessInstance historicProcessInstance = deployer.getHistoricDataService().findHistoricProcessInstance(processInstance.getId());

      assertNotNull(historicProcessInstance);
      assertEquals(processInstance.getId(), historicProcessInstance.getProcessInstanceId());
      assertEquals(processInstance.getProcessDefinitionId(), historicProcessInstance.getProcessDefinitionId());
      assertNotNull(historicProcessInstance.getStartTime());
      assertNull(historicProcessInstance.getEndTime());
      assertNull(historicProcessInstance.getDurationInMillis());

      // TODO: check for HistoricActivityInstance created once events get fired

      List<Task> tasks = deployer.getTaskService().createTaskQuery().processInstance(processInstance.getId()).list();

      assertEquals(1, tasks.size());

      deployer.getTaskService().complete(tasks.get(0).getId());

      historicProcessInstance = deployer.getHistoricDataService().findHistoricProcessInstance(processInstance.getId());

      assertNotNull(historicProcessInstance);
      assertEquals(processInstance.getId(), historicProcessInstance.getProcessInstanceId());
      assertEquals(processInstance.getProcessDefinitionId(), historicProcessInstance.getProcessDefinitionId());
      assertNotNull(historicProcessInstance.getStartTime());
      assertNotNull(historicProcessInstance.getEndTime());
      assertNotNull(historicProcessInstance.getDurationInMillis());

      // TODO: check for HistoricActivityInstance updated once events get fired
    } finally {
      deployer.getCommandExecutor().execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          commandContext.getPersistenceSession().deleteHistoricProcessInstance(processInstance.getId());
          return null;
        }
      });
    }
  }
}
