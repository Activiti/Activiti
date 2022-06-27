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


package org.activiti.engine.test.bpmn.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.InputStream;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;
import org.activiti.validation.validator.Problems;

/**


 */
public class BpmnDeploymentTest extends PluggableActivitiTestCase {

  @Deployment
  public void testGetBpmnXmlFileThroughService() {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);

    // verify bpmn file name
    assertThat(deploymentResources).hasSize(1);
    String bpmnResourceName = "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    assertThat(deploymentResources.get(0)).isEqualTo(bpmnResourceName);

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition.getResourceName()).isEqualTo(bpmnResourceName);
    assertThat(processDefinition.getDiagramResourceName()).isNull();
    assertThat(processDefinition.hasStartFormKey()).isFalse();

    ProcessDefinition readOnlyProcessDefinition = ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinition.getId());
    assertThat(readOnlyProcessDefinition.getDiagramResourceName()).isNull();

    // verify content
    InputStream deploymentInputStream = repositoryService.getResourceAsStream(deploymentId, bpmnResourceName);
    String contentFromDeployment = readInputStreamToString(deploymentInputStream);
    assertThat(contentFromDeployment.length() > 0).isTrue();
    assertThat(contentFromDeployment.contains("process id=\"emptyProcess\"")).isTrue();

    InputStream fileInputStream = ReflectUtil.getResourceAsStream("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml");
    String contentFromFile = readInputStreamToString(fileInputStream);
    assertThat(contentFromDeployment).isEqualTo(contentFromFile);
  }

  private String readInputStreamToString(InputStream inputStream) {
    byte[] bytes = IoUtil.readInputStream(inputStream, "input stream");
    return new String(bytes);
  }

  public void testViolateBPMNIdMaximumLength() {
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> {
        repositoryService.createDeployment()
          .addClasspathResource("org/activiti/engine/test/bpmn/deployment/definitionWithLongTargetNamespace.bpmn20.xml")
          .deploy();
      })
      .withMessageContaining(Problems.BPMN_MODEL_TARGET_NAMESPACE_TOO_LONG);

    // Verify that nothing is deployed
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(0);
  }


  public void testViolateProcessDefinitionIdMaximumLength() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> {
        repositoryService.createDeployment()
          .addClasspathResource("org/activiti/engine/test/bpmn/deployment/processWithLongId.bpmn20.xml")
          .deploy();
      }).withMessageContaining(Problems.PROCESS_DEFINITION_ID_TOO_LONG);

    // Verify that nothing is deployed
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(0);
  }

  public void testViolateProcessDefinitionNameAndDescriptionMaximumLength() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> {
        repositoryService.createDeployment()
          .addClasspathResource("org/activiti/engine/test/bpmn/deployment/processWithLongNameAndDescription.bpmn20.xml")
          .deploy();
      })
      .withMessageContaining(Problems.PROCESS_DEFINITION_NAME_TOO_LONG)
      .withMessageContaining(Problems.PROCESS_DEFINITION_DOCUMENTATION_TOO_LONG);

    // Verify that nothing is deployed
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(0);
  }

  public void testViolateDefinitionTargetNamespaceMaximumLength() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> {
        repositoryService.createDeployment()
          .addClasspathResource("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.definitionWithLongTargetNamespace.bpmn20.xml")
          .deploy();
      })
      .withMessageContaining(Problems.BPMN_MODEL_TARGET_NAMESPACE_TOO_LONG);

    // Verify that nothing is deployed
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(0);
  }

  public void testDeploySameFileTwice() {
    String bpmnResourceName = "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    repositoryService.createDeployment().enableDuplicateFiltering().addClasspathResource(bpmnResourceName).name("twice").deploy();

    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);

    // verify bpmn file name
    assertThat(deploymentResources).hasSize(1);
    assertThat(deploymentResources.get(0)).isEqualTo(bpmnResourceName);

    repositoryService.createDeployment().enableDuplicateFiltering().addClasspathResource(bpmnResourceName).name("twice").deploy();
    List<org.activiti.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    assertThat(deploymentList).hasSize(1);

    repositoryService.deleteDeployment(deploymentId);
  }

  public void testDeployTwoProcessesWithDuplicateIdAtTheSameTime() {
    String bpmnResourceName = "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    String bpmnResourceName2 = "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService2.bpmn20.xml";
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> {
        repositoryService.createDeployment().enableDuplicateFiltering()
          .addClasspathResource(bpmnResourceName).addClasspathResource(bpmnResourceName2).name("duplicateAtTheSameTime")
          .deploy();
      });

    // Verify that nothing is deployed
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(0);
  }

  public void testDeployDifferentFiles() {
    String bpmnResourceName = "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    repositoryService.createDeployment().enableDuplicateFiltering().addClasspathResource(bpmnResourceName).name("twice").deploy();

    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);

    // verify bpmn file name
    assertThat(deploymentResources).hasSize(1);
    assertThat(deploymentResources.get(0)).isEqualTo(bpmnResourceName);

    bpmnResourceName = "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml";
    repositoryService.createDeployment().enableDuplicateFiltering().addClasspathResource(bpmnResourceName).name("twice").deploy();
    List<org.activiti.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    assertThat(deploymentList).hasSize(2);

    for (org.activiti.engine.repository.Deployment deployment : deploymentList) {
      repositoryService.deleteDeployment(deployment.getId());
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml",
      "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg" })
  public void testProcessDiagramResource() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    assertThat(processDefinition.getResourceName()).isEqualTo("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml");
    BpmnModel processModel = repositoryService.getBpmnModel(processDefinition.getId());
    List<StartEvent> startEvents = processModel.getMainProcess().findFlowElementsOfType(StartEvent.class);
    assertThat(startEvents).hasSize(1);
    assertThat(startEvents.get(0).getFormKey()).isEqualTo("someFormKey");

    String diagramResourceName = processDefinition.getDiagramResourceName();
    assertThat(diagramResourceName).isEqualTo("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg");

    InputStream diagramStream = repositoryService.getResourceAsStream(deploymentIdFromDeploymentAnnotation,
        "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg");
    byte[] diagramBytes = IoUtil.readInputStream(diagramStream, "diagram stream");
    assertThat(diagramBytes.length).isEqualTo(33343);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.bpmn20.xml",
      "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.a.jpg",
      "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.b.jpg",
      "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.c.jpg" })
  public void testMultipleDiagramResourcesProvided() {
    ProcessDefinition processA = repositoryService.createProcessDefinitionQuery().processDefinitionKey("a").singleResult();
    ProcessDefinition processB = repositoryService.createProcessDefinitionQuery().processDefinitionKey("b").singleResult();
    ProcessDefinition processC = repositoryService.createProcessDefinitionQuery().processDefinitionKey("c").singleResult();

    assertThat(processA.getDiagramResourceName()).isEqualTo("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.a.jpg");
    assertThat(processB.getDiagramResourceName()).isEqualTo("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.b.jpg");
    assertThat(processC.getDiagramResourceName()).isEqualTo("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.c.jpg");
  }

  @Deployment
  public void testProcessDefinitionDescription() {
    String id = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    ProcessDefinition processDefinition = ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(id);
    assertThat(processDefinition.getDescription()).isEqualTo("This is really good process documentation!");
  }

  public void testDeploySameFileTwiceForDifferentTenantId() {
    String bpmnResourceName = "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    repositoryService.createDeployment().enableDuplicateFiltering().addClasspathResource(bpmnResourceName).name("twice").tenantId("Tenant_A").deploy();

    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);

    // verify bpmn file name
    assertThat(deploymentResources).hasSize(1);
    assertThat(deploymentResources.get(0)).isEqualTo(bpmnResourceName);

    repositoryService.createDeployment().enableDuplicateFiltering().addClasspathResource(bpmnResourceName).name("twice").tenantId("Tenant_B").deploy();
    List<org.activiti.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    // Now, we should have two deployment for same process file, one for
    // each tenant
    assertThat(deploymentList).hasSize(2);

    for (org.activiti.engine.repository.Deployment deployment : deploymentList) {
      repositoryService.deleteDeployment(deployment.getId());
    }
  }

  public void testProcessDefinitionShouldHasStartFormKey() {
      BpmnModel bpmnModel = createOneTaskAndStartEventWithFormKeyProcess();

      org.activiti.engine.repository.Deployment deployment = repositoryService.createDeployment().addBpmnModel("test.bpmn20.xml", bpmnModel).deploy();
      ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();

      assertThat(processDefinition.hasStartFormKey()).isTrue();

      repositoryService.deleteDeployment(deployment.getId());
  }

}
