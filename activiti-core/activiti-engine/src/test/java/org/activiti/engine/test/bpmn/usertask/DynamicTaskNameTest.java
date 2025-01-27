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


package org.activiti.engine.test.bpmn.usertask;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


/**

 */
public class DynamicTaskNameTest extends PluggableActivitiTestCase {

  @Deployment(resources={"org/activiti/engine/test/bpmn/usertask/DynamicTaskNameTest.setTaskName.bpmn20.xml"})
  public void testDynamicallySetTaskName() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dynamicTaskName");
    String processInstanceId = processInstance.getId();

    final Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    final HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).singleResult();

    assertThat(task.getName()).startsWith("Dynamic task");
    assertThat(task.getName()).isEqualTo(historicTask.getName());
  }

}
