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


package org.activiti.engine.test.bpmn.servicetask;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.test.Deployment;

/**
 *

 */
public class ServiceTaskVariablesTest extends PluggableActivitiTestCase {

  static boolean isOkInDelegate2;
  static boolean isOkInDelegate3;

  public static class Variable implements Serializable {
    private static final long serialVersionUID = 1L;
    public String value;
  }

  public static class Delegate1 implements JavaDelegate {

    public void execute(DelegateExecution execution) {
      Variable v = new Variable();
      v.value = "delegate1";
      execution.setVariable("variable", v);
    }

  }

  public static class Delegate2 implements JavaDelegate {

    public void execute(DelegateExecution execution) {
      Variable v = (Variable) execution.getVariable("variable");
      synchronized (ServiceTaskVariablesTest.class) {
        // we expect this to be 'true'
        isOkInDelegate2 = (v.value != null && v.value.equals("delegate1"));
      }
      v.value = "delegate2";
      execution.setVariable("variable", v);
    }

  }

  public static class Delegate3 implements JavaDelegate {

    public void execute(DelegateExecution execution) {
      Variable v = (Variable) execution.getVariable("variable");
      synchronized (ServiceTaskVariablesTest.class) {
        // we expect this to be 'true' as well
        isOkInDelegate3 = (v.value != null && v.value.equals("delegate2"));
      }
    }

  }

  @Deployment
  public void testSerializedVariablesBothAsync() {

    // in this test, there is an async cont. both before the second and the
    // third service task in the sequence

    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService.createJobQuery().singleResult();
    assertThat(job).isNotNull();
    managementService.executeJob(job.getId());

    job = managementService.createJobQuery().singleResult();
    assertThat(job).isNotNull();
    managementService.executeJob(job.getId());

    assertThat(isOkInDelegate2).isTrue();
    assertThat(isOkInDelegate3).isTrue();
  }

  @Deployment
  public void testSerializedVariablesThirdAsync() {

    // in this test, only the third service task is async

    runtimeService.startProcessInstanceByKey("process");
    waitForJobExecutorToProcessAllJobs(10000, 500);

    synchronized (ServiceTaskVariablesTest.class) {
      assertThat(isOkInDelegate2).isTrue();
      assertThat(isOkInDelegate3).isTrue();
    }

  }

}
