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
package org.activiti.engine.test.api.variables;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class SerializableVariableTest extends PluggableActivitiTestCase {

  @Deployment
  public void testUpdateSerializableInServiceTask() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("myVar", new TestSerializableVariable(1));
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testUpdateSerializableInServiceTask", vars);

    // There is a task here, such the VariableInstanceEntityImpl is inserter first, and updated later
    // (instead of being inserted/updated in the same Tx)
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());

    TestSerializableVariable testSerializableVariable = (TestSerializableVariable) runtimeService.getVariable(processInstance.getId(), "myVar");
    assertThat(testSerializableVariable.getNumber()).isEqualTo(2);
  }

  public static class TestUpdateSerializableVariableDelegate implements JavaDelegate {

    public void execute(DelegateExecution execution) {
      TestSerializableVariable var = (TestSerializableVariable) execution.getVariable("myVar");
      var.setNumber(2);
    }

  }

  public static class TestSerializableVariable implements Serializable {

    private static final long serialVersionUID = 1L;
    private int number;

    public TestSerializableVariable(int number) {
      this.number = number;
    }

    public int getNumber() {
      return number;
    }

    public void setNumber(int number) {
      this.number = number;
    }

  }

}
