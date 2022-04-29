/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import java.util.List;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class HistoricProcessInstanceQueryTest extends PluggableActivitiTestCase {

  @Deployment
  public void testLocalization() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("historicProcessLocalization");
    String processInstanceId = processInstance.getId();
    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    taskService.complete(task.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricProcessInstance> processes = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).list();
      assertThat(processes).hasSize(1);
      assertThat(processes.get(0).getName()).isNull();
      assertThat(processes.get(0).getDescription()).isNull();

      ObjectNode infoNode = dynamicBpmnService.changeLocalizationName("en-GB", "historicProcessLocalization", "Historic Process Name 'en-GB'");
      dynamicBpmnService.changeLocalizationDescription("en-GB", "historicProcessLocalization", "Historic Process Description 'en-GB'", infoNode);
      dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);

      dynamicBpmnService.changeLocalizationName("en", "historicProcessLocalization", "Historic Process Name 'en'", infoNode);
      dynamicBpmnService.changeLocalizationDescription("en", "historicProcessLocalization", "Historic Process Description 'en'", infoNode);
      dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);

      processes = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).list();
      assertThat(processes).hasSize(1);
      assertThat(processes.get(0).getName()).isNull();
      assertThat(processes.get(0).getDescription()).isNull();

      processes = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).locale("en-GB").list();
      assertThat(processes).hasSize(1);
      assertThat(processes.get(0).getName()).isEqualTo("Historic Process Name 'en-GB'");
      assertThat(processes.get(0).getDescription()).isEqualTo("Historic Process Description 'en-GB'");

      processes = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).listPage(0,10);
      assertThat(processes).hasSize(1);
      assertThat(processes.get(0).getName()).isNull();
      assertThat(processes.get(0).getDescription()).isNull();

      processes = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).locale("en-GB").listPage(0,10);
      assertThat(processes).hasSize(1);
      assertThat(processes.get(0).getName()).isEqualTo("Historic Process Name 'en-GB'");
      assertThat(processes.get(0).getDescription()).isEqualTo("Historic Process Description 'en-GB'");

      HistoricProcessInstance process = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
      assertThat(process.getName()).isNull();
      assertThat(process.getDescription()).isNull();

      process = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).locale("en-GB").singleResult();
      assertThat(process.getName()).isEqualTo("Historic Process Name 'en-GB'");
      assertThat(process.getDescription()).isEqualTo("Historic Process Description 'en-GB'");

      process = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).locale("en").singleResult();
      assertThat(process.getName()).isEqualTo("Historic Process Name 'en'");
      assertThat(process.getDescription()).isEqualTo("Historic Process Description 'en'");

      process = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).locale("en-AU").withLocalizationFallback().singleResult();
      assertThat(process.getName()).isEqualTo("Historic Process Name 'en'");
      assertThat(process.getDescription()).isEqualTo("Historic Process Description 'en'");
    }
  }
}
