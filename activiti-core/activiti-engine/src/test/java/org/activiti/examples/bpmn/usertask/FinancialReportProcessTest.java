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
package org.activiti.examples.bpmn.usertask;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class FinancialReportProcessTest extends PluggableActivitiTestCase {


  private static final String KERMIT = "kermit";
  private static final List<String> KERMITSGROUPS = asList("management");

  private static final String FOZZIE = "fozzie";
  private static final List<String> FOZZIESGROUPS = asList("accountancy");


  @Deployment(resources = { "org/activiti/examples/bpmn/usertask/FinancialReportProcess.bpmn20.xml" })
  public void testProcess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("financialReport");

    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("fozzie",FOZZIESGROUPS).list();
    assertThat(tasks).hasSize(1);
    Task task = tasks.get(0);
    assertThat(task.getName()).isEqualTo("Write monthly financial report");

    taskService.claim(task.getId(), FOZZIE);
    tasks = taskService.createTaskQuery().taskAssignee(FOZZIE).list();

    assertThat(tasks).hasSize(1);
    taskService.complete(task.getId());

    tasks = taskService.createTaskQuery().taskCandidateUser(FOZZIE,FOZZIESGROUPS).list();
    assertThat(tasks).hasSize(0);
    tasks = taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("Verify monthly financial report");
    taskService.complete(tasks.get(0).getId());

    assertProcessEnded(processInstance.getId());
  }

}
