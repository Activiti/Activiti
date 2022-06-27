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

import org.activiti.engine.impl.test.PluggableActivitiTestCase;

public class ExecutionAndProcessInstanceQueryVersionTest extends PluggableActivitiTestCase {

  private static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";
  private static final String DEPLOYMENT_FILE_PATH = "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml";

  private org.activiti.engine.repository.Deployment oldDeployment;
  private org.activiti.engine.repository.Deployment newDeployment;

  protected void setUp() throws Exception {
    super.setUp();
    oldDeployment = repositoryService.createDeployment()
      .addClasspathResource(DEPLOYMENT_FILE_PATH)
      .deploy();

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();

    newDeployment = repositoryService.createDeployment()
          .addClasspathResource(DEPLOYMENT_FILE_PATH)
          .deploy();

    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
  }

  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(oldDeployment.getId(), true);
    repositoryService.deleteDeployment(newDeployment.getId(), true);
  }

  public void testProcessInstanceQueryByProcessDefinitionVersion() {
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionVersion(1).count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionVersion(2).count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionVersion(3).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionVersion(1).count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionVersion(2).list()).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionVersion(3).list()).hasSize(0);
  }

  public void testProcessInstanceQueryByProcessDefinitionVersionAndKey() {
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(1).count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(2).count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("undefined").processDefinitionVersion(1).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("undefined").processDefinitionVersion(2).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(1).list()).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(2).list()).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("undefined").processDefinitionVersion(1).list()).hasSize(0);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("undefined").processDefinitionVersion(2).list()).hasSize(0);
  }

  public void testProcessInstanceOrQueryByProcessDefinitionVersion() {
    assertThat(runtimeService.createProcessInstanceQuery().or().processDefinitionVersion(1).processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().or().processDefinitionVersion(2).processDefinitionId("undefined").endOr().count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().or().processDefinitionVersion(3).processDefinitionId("undefined").endOr().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().or().processDefinitionVersion(1).processDefinitionId("undefined").endOr().list()).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().or().processDefinitionVersion(2).processDefinitionId("undefined").endOr().list()).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().or().processDefinitionVersion(3).processDefinitionId("undefined").endOr().list()).hasSize(0);
  }

  public void testExecutionQueryByProcessDefinitionVersion() {
    assertThat(runtimeService.createExecutionQuery().processDefinitionVersion(1).count()).isEqualTo(2);
    assertThat(runtimeService.createExecutionQuery().processDefinitionVersion(2).count()).isEqualTo(2);
    assertThat(runtimeService.createExecutionQuery().processDefinitionVersion(3).count()).isEqualTo(0);
    assertThat(runtimeService.createExecutionQuery().processDefinitionVersion(1).list()).hasSize(2);
    assertThat(runtimeService.createExecutionQuery().processDefinitionVersion(2).list()).hasSize(2);
    assertThat(runtimeService.createExecutionQuery().processDefinitionVersion(3).list()).hasSize(0);
  }

  public void testExecutionQueryByProcessDefinitionVersionAndKey() {
    assertThat(runtimeService.createExecutionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(1).count()).isEqualTo(2);
    assertThat(runtimeService.createExecutionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(2).count()).isEqualTo(2);
    assertThat(runtimeService.createExecutionQuery().processDefinitionKey("undefined").processDefinitionVersion(1).count()).isEqualTo(0);
    assertThat(runtimeService.createExecutionQuery().processDefinitionKey("undefined").processDefinitionVersion(2).count()).isEqualTo(0);
    assertThat(runtimeService.createExecutionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(1).list()).hasSize(2);
    assertThat(runtimeService.createExecutionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionVersion(2).list()).hasSize(2);
    assertThat(runtimeService.createExecutionQuery().processDefinitionKey("undefined").processDefinitionVersion(1).list()).hasSize(0);
    assertThat(runtimeService.createExecutionQuery().processDefinitionKey("undefined").processDefinitionVersion(2).list()).hasSize(0);
  }
}
