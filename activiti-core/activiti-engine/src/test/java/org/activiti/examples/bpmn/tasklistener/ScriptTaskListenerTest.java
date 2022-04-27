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

package org.activiti.examples.bpmn.tasklistener;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class ScriptTaskListenerTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/examples/bpmn/tasklistener/ScriptTaskListenerTest.bpmn20.xml" })
  public void testScriptTaskListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("scriptTaskListenerProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).as("Name does not match").isEqualTo("All your base are belong to us");

    taskService.complete(task.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
      assertThat(historicTask.getOwner()).isEqualTo("kermit");

      task = taskService.createTaskQuery().singleResult();
      assertThat(task.getName()).as("Task name not set with 'bar' variable").isEqualTo("BAR");
    }

    Object bar = runtimeService.getVariable(processInstance.getId(), "bar");
    assertThat(bar).as("Expected 'bar' variable to be local to script").isNull();

    Object foo = runtimeService.getVariable(processInstance.getId(), "foo");
    assertThat(foo).as("Could not find the 'foo' variable in variable scope").isEqualTo("FOO");
  }

}
