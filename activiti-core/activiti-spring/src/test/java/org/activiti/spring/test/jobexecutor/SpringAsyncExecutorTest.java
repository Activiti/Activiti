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
package org.activiti.spring.test.jobexecutor;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.impl.test.CleanTestExecutionListener;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(CleanTestExecutionListener.class)
@ContextConfiguration("classpath:org/activiti/spring/test/components/SpringjobExecutorTest-context.xml")
public class SpringAsyncExecutorTest extends SpringActivitiTestCase {

  @Autowired
  protected ManagementService managementService;

  @Autowired
  protected RuntimeService runtimeService;

  @Autowired
  protected TaskService taskService;

  @Test
  public void testHappyJobExecutorPath() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process1");
    assertThat(instance).isNotNull();
    waitForTasksToExpire();

    List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
    assertThat(activeTasks.isEmpty()).isTrue();
  }

  @Test
  public void testRollbackJobExecutorPath() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("errorProcess1");
    assertThat(instance).isNotNull();
    waitForTasksToExpire();

    List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
    assertThat(activeTasks.size() == 1).isTrue();
  }

  private void waitForTasksToExpire() throws Exception {
    boolean finished = false;
    int nrOfSleeps = 0;
    while (!finished) {
      long jobCount = managementService.createJobQuery().count();
      long timerCount = managementService.createTimerJobQuery().count();
      if (jobCount == 0 && timerCount == 0) {
        finished = true;
      } else if (nrOfSleeps < 20){
        nrOfSleeps++;
        Thread.sleep(500L);
      } else {
        finished = true;
      }
    }
  }

}
