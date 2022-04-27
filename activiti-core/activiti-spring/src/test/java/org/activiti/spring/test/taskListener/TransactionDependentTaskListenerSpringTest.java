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


package org.activiti.spring.test.taskListener;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration("classpath:org/activiti/spring/test/taskListener/TaskListenerDelegateExpressionTest-context.xml")
public class TransactionDependentTaskListenerSpringTest extends SpringActivitiTestCase {

  @Autowired
  MyTransactionDependentTaskListener listener;

  @Deployment
  public void testCustomPropertiesMapDelegateExpression() {
    runtimeService.startProcessInstanceByKey("transactionDependentTaskListenerProcess");

    // Completing first task will trigger the first closed listener (expression custom properties resolver)
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertThat(listener.getCurrentTasks().get(0).getTaskId()).isEqualTo("task3");
    assertThat(listener.getCurrentTasks().get(0).getCustomPropertiesMap().get("customProp1")).isEqualTo("task3");

    // Completing second task will trigger the second closed listener (delegate expression custom properties resolver)
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertThat(listener.getCurrentTasks().get(1).getTaskId()).isEqualTo("task4");
    assertThat(listener.getCurrentTasks().get(1).getCustomPropertiesMap().get("customProp1")).isEqualTo("task4");
  }

}
