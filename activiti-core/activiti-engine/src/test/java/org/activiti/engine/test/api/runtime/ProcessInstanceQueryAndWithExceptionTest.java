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
package org.activiti.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.runtime.TimerJobQuery;

public class ProcessInstanceQueryAndWithExceptionTest extends PluggableActivitiTestCase {

  private static final String PROCESS_DEFINITION_KEY_NO_EXCEPTION = "oneTaskProcess";
  private static final String PROCESS_DEFINITION_KEY_WITH_EXCEPTION_1 = "JobErrorCheck";
  private static final String PROCESS_DEFINITION_KEY_WITH_EXCEPTION_2 = "JobErrorDoubleCheck";

  private org.activiti.engine.repository.Deployment deployment;

  protected void setUp() throws Exception {
    super.setUp();
    deployment = repositoryService.createDeployment()
          .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
          .addClasspathResource("org/activiti/engine/test/api/runtime/JobErrorCheck.bpmn20.xml")
          .addClasspathResource("org/activiti/engine/test/api/runtime/JobErrorDoubleCheck.bpmn20.xml")
          .deploy();
  }

  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deployment.getId(), true);
    super.tearDown();
  }

  public void testQueryWithException() throws InterruptedException{
    ProcessInstance processNoException = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_NO_EXCEPTION);

    ProcessInstanceQuery queryNoException = runtimeService.createProcessInstanceQuery();
    assertThat(queryNoException.count()).isEqualTo(1);
    assertThat(queryNoException.list()).hasSize(1);
    assertThat(queryNoException.list().get(0).getId()).isEqualTo(processNoException.getId());

    ProcessInstanceQuery queryWithException = runtimeService.createProcessInstanceQuery();
    assertThat(queryWithException.withJobException().count()).isEqualTo(0);
    assertThat(queryWithException.withJobException().list()).hasSize(0);

    ProcessInstance processWithException1 = startProcessInstanceWithFailingJob(PROCESS_DEFINITION_KEY_WITH_EXCEPTION_1);
    TimerJobQuery jobQuery1 = managementService.createTimerJobQuery().processInstanceId(processWithException1.getId());
    assertThat(jobQuery1.withException().count()).isEqualTo(1);
    assertThat(jobQuery1.withException().list()).hasSize(1);
    assertThat(queryWithException.withJobException().count()).isEqualTo(1);
    assertThat(queryWithException.withJobException().list()).hasSize(1);
    assertThat(queryWithException.withJobException().list().get(0).getId()).isEqualTo(processWithException1.getId());

    ProcessInstance processWithException2 = startProcessInstanceWithFailingJob(PROCESS_DEFINITION_KEY_WITH_EXCEPTION_2);
    TimerJobQuery jobQuery2 = managementService.createTimerJobQuery().processInstanceId(processWithException2.getId());
    assertThat(jobQuery2.withException().count()).isEqualTo(2);
    assertThat(jobQuery2.withException().list()).hasSize(2);

    assertThat(queryWithException.withJobException().count()).isEqualTo(2);
    assertThat(queryWithException.withJobException().list()).hasSize(2);
    assertThat(queryWithException.withJobException().processDefinitionKey(PROCESS_DEFINITION_KEY_WITH_EXCEPTION_1).list().get(0).getId()).isEqualTo(processWithException1.getId());
    assertThat(queryWithException.withJobException().processDefinitionKey(PROCESS_DEFINITION_KEY_WITH_EXCEPTION_2).list().get(0).getId()).isEqualTo(processWithException2.getId());
  }

  private ProcessInstance startProcessInstanceWithFailingJob(String processInstanceByKey) {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processInstanceByKey);

    List<Job> jobList = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .list();

    for (Job job : jobList) {
      assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> managementService.executeJob(job.getId()));
    }
    return processInstance;
  }

  // Test delegate
  public static class TestJavaDelegate implements JavaDelegate {
    public void execute(DelegateExecution execution){
      throw new RuntimeException();
    }
  }
}
