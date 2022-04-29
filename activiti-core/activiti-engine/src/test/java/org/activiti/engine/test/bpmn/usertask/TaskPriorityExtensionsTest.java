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


package org.activiti.engine.test.bpmn.usertask;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class TaskPriorityExtensionsTest extends PluggableActivitiTestCase {

  @Deployment
  public void testPriorityExtension() throws Exception {
    testPriorityExtension(25);
    testPriorityExtension(75);
  }

  private void testPriorityExtension(int priority) throws Exception {
    final Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("taskPriority", priority);

    // Start process-instance, passing priority that should be used as task
    // priority
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskPriorityExtension", variables);

    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    assertThat(task.getPriority()).isEqualTo(priority);
  }

  @Deployment
  public void testPriorityExtensionString() throws Exception {
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskPriorityExtensionString");
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task.getPriority()).isEqualTo(42);
  }
}
