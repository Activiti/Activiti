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

package org.activiti.standalone.validation;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class DisabledSchemaValidationTest {

  protected ProcessEngine processEngine;

  protected RepositoryService repositoryService;

  @Before
  public void setup() {
    this.processEngine = new StandaloneInMemProcessEngineConfiguration()
      .setProcessEngineName(this.getClass().getName())
      .setJdbcUrl("jdbc:h2:mem:activiti-process-validation;DB_CLOSE_DELAY=1000")
      .buildProcessEngine();
    this.repositoryService = processEngine.getRepositoryService();
  }

  @After
  public void tearDown() {
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId());
    }

    processEngine.close();
    ProcessEngines.unregister(processEngine);
    processEngine = null;
    repositoryService = null;
  }

  @Test
  public void testDisableValidation() {

    // Should fail
    assertThatExceptionOfType(XMLException.class)
      .isThrownBy(() -> repositoryService.createDeployment().addClasspathResource("org/activiti/standalone/validation/invalid_process_xsd_error.bpmn20.xml").deploy());

    // Should fail with validation errors
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> repositoryService.createDeployment().addClasspathResource("org/activiti/standalone/validation/invalid_process_xsd_error.bpmn20.xml").disableSchemaValidation().deploy());

  }

}
