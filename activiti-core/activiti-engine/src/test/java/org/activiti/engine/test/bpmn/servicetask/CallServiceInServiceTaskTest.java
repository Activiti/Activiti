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

package org.activiti.engine.test.bpmn.servicetask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


public class CallServiceInServiceTaskTest extends PluggableActivitiTestCase {

  @Deployment
  public void testStartProcessFromDelegate() {
    runtimeService.startProcessInstanceByKey("startProcessFromDelegate");

    // Starting the process should lead to two processes being started,
    // The other one started from the java delegate in the service task
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    assertThat(processInstances).hasSize(2);

    boolean startProcessFromDelegateFound = false;
    boolean oneTaskProcessFound = false;
    for (ProcessInstance processInstance : processInstances) {
      ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());
      if (processDefinition.getKey().equals("startProcessFromDelegate")) {
        startProcessFromDelegateFound = true;
      } else if (processDefinition.getKey().equals("oneTaskProcess")) {
        oneTaskProcessFound = true;
      }
    }

    assertThat(startProcessFromDelegateFound).isTrue();
    assertThat(oneTaskProcessFound).isTrue();
  }

  @Deployment
  public void testRollBackOnException() {
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("startProcessFromDelegate"));

    // Starting the process should cause a rollback of both processes
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

}
