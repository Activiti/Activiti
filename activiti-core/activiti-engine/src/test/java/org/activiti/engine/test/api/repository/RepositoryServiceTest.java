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


package org.activiti.engine.test.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;

/**
 */
public class RepositoryServiceTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testStartProcessInstanceById() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertThat(processDefinitions).hasSize(1);

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertThat(processDefinition.getKey()).isEqualTo("oneTaskProcess");
    assertThat(processDefinition.getId()).isNotNull();
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testFindProcessDefinitionById() {
    List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery().list();
    assertThat(definitions).hasSize(1);

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(definitions.get(0).getId()).singleResult();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(processDefinition).isNotNull();
    assertThat(processDefinition.getKey()).isEqualTo("oneTaskProcess");
    assertThat(processDefinition.getName()).isEqualTo("The One Task Process");

    processDefinition = repositoryService.getProcessDefinition(definitions.get(0).getId());
    assertThat(processDefinition.getDescription()).isEqualTo("This is a process for testing purposes");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteDeploymentWithRunningInstances() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertThat(processDefinitions).hasSize(1);
    ProcessDefinition processDefinition = processDefinitions.get(0);

    runtimeService.startProcessInstanceById(processDefinition.getId());

    // Exception expected when deleting deployment with running process
    assertThatExceptionOfType(RuntimeException.class)
      .isThrownBy(() -> repositoryService.deleteDeployment(processDefinition.getDeploymentId()));
  }

  public void testDeleteDeploymentNullDeploymentId() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.deleteDeployment(null))
      .withMessageContaining("deploymentId is null");
  }

  public void testDeleteDeploymentCascadeNullDeploymentId() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.deleteDeployment(null, true))
      .withMessageContaining("deploymentId is null");
  }

  public void testDeleteDeploymentNonExistentDeploymentId() {
    assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
      .isThrownBy(() -> repositoryService.deleteDeployment("foobar"))
      .withMessageContaining("Could not find a deployment with id 'foobar'.");
  }

  public void testDeleteDeploymentCascadeNonExistentDeploymentId() {
    assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
      .isThrownBy(() -> repositoryService.deleteDeployment("foobar", true))
      .withMessageContaining("Could not find a deployment with id 'foobar'.");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteDeploymentCascadeWithRunningInstances() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertThat(processDefinitions).hasSize(1);
    ProcessDefinition processDefinition = processDefinitions.get(0);

    runtimeService.startProcessInstanceById(processDefinition.getId());

    // Try to delete the deployment, no exception should be thrown
    repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
  }

  public void testFindDeploymentResourceNamesNullDeploymentId() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.getDeploymentResourceNames(null))
      .withMessageContaining("deploymentId is null");
  }

  public void testDeploymentWithDelayedProcessDefinitionActivation() {

    Date startTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startTime);
    Date inThreeDays = new Date(startTime.getTime() + (3 * 24 * 60 * 60 * 1000));

    // Deploy process, but activate after three days
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml").activateProcessDefinitionsOn(inThreeDays).deploy();

    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(2);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(2);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(0);

    // Shouldn't be able to start a process instance
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("oneTaskProcess"))
      .withMessageContaining("suspended");

    // Move time four days forward, the timer will fire and the process
    // definitions will be active
    Date inFourDays = new Date(startTime.getTime() + (4 * 24 * 60 * 60 * 1000));
    processEngineConfiguration.getClock().setCurrentTime(inFourDays);
    waitForJobExecutorToProcessAllJobs(5000L);

    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(2);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(0);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(2);

    // Should be able to start process instance
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    // Cleanup
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testGetResourceAsStreamUnexistingResourceInExistingDeployment() {
    // Get hold of the deployment id
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
      .isThrownBy(() -> repositoryService.getResourceAsStream(deployment.getId(), "org/activiti/engine/test/api/unexistingProcess.bpmn.xml"))
      .withMessageContaining("no resource found with name")
      .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(InputStream.class));
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testGetResourceAsStreamUnexistingDeployment() {
    assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
      .isThrownBy(() -> repositoryService.getResourceAsStream("unexistingdeployment", "org/activiti/engine/test/api/unexistingProcess.bpmn.xml"))
      .withMessageContaining("deployment does not exist")
      .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(org.activiti.engine.repository.Deployment.class));
  }

  public void testGetResourceAsStreamNullArguments() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.getResourceAsStream(null, "resource"))
      .withMessageContaining("deploymentId is null");

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.getResourceAsStream("deployment", null))
      .withMessageContaining("resourceName is null");
  }

  public void testNewModelPersistence() {
    Model model = repositoryService.newModel();
    assertThat(model).isNotNull();

    model.setName("Test model");
    model.setCategory("test");
    model.setMetaInfo("meta");
    repositoryService.saveModel(model);

    assertThat(model.getId()).isNotNull();
    model = repositoryService.getModel(model.getId());
    assertThat(model).isNotNull();
    assertThat(model.getName()).isEqualTo("Test model");
    assertThat(model.getCategory()).isEqualTo("test");
    assertThat(model.getMetaInfo()).isEqualTo("meta");
    assertThat(model.getCreateTime()).isNotNull();
    assertThat(model.getVersion()).isEqualTo(Integer.valueOf(1));

    repositoryService.deleteModel(model.getId());
  }

  public void testNewModelWithSource() throws Exception {
    Model model = repositoryService.newModel();
    model.setName("Test model");
    byte[] testSource = "modelsource".getBytes("utf-8");
    repositoryService.saveModel(model);

    assertThat(model.getId()).isNotNull();
    repositoryService.addModelEditorSource(model.getId(), testSource);

    model = repositoryService.getModel(model.getId());
    assertThat(model).isNotNull();
    assertThat(model.getName()).isEqualTo("Test model");

    byte[] editorSourceBytes = repositoryService.getModelEditorSource(model.getId());
    assertThat(new String(editorSourceBytes, "utf-8")).isEqualTo("modelsource");

    repositoryService.deleteModel(model.getId());
  }

  public void testUpdateModelPersistence() throws Exception {
    Model model = repositoryService.newModel();
    assertThat(model).isNotNull();

    model.setName("Test model");
    model.setCategory("test");
    model.setMetaInfo("meta");
    repositoryService.saveModel(model);

    assertThat(model.getId()).isNotNull();
    model = repositoryService.getModel(model.getId());
    assertThat(model).isNotNull();

    model.setName("New name");
    model.setCategory("New category");
    model.setMetaInfo("test");
    model.setVersion(2);
    repositoryService.saveModel(model);

    assertThat(model.getId()).isNotNull();
    repositoryService.addModelEditorSource(model.getId(), "new".getBytes("utf-8"));
    repositoryService.addModelEditorSourceExtra(model.getId(), "new".getBytes("utf-8"));

    model = repositoryService.getModel(model.getId());

    assertThat(model.getName()).isEqualTo("New name");
    assertThat(model.getCategory()).isEqualTo("New category");
    assertThat(model.getMetaInfo()).isEqualTo("test");
    assertThat(model.getCreateTime()).isNotNull();
    assertThat(model.getVersion()).isEqualTo(Integer.valueOf(2));
    assertThat(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8")).isEqualTo("new");
    assertThat(new String(repositoryService.getModelEditorSourceExtra(model.getId()), "utf-8")).isEqualTo("new");

    repositoryService.deleteModel(model.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testProcessDefinitionEntitySerializable() throws Exception {
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    ProcessDefinition processDefinition = repositoryService.getProcessDefinition(procDefId);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(processDefinition);

    byte[] bytes = baos.toByteArray();
    assertThat(bytes).isNotEmpty();
  }

  @Deployment
  public void testGetBpmnModel() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // Some basic assertions
    BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
    assertThat(bpmnModel).isNotNull();
    assertThat(bpmnModel.getProcesses()).hasSize(1);
    assertThat(!bpmnModel.getLocationMap().isEmpty()).isTrue();
    assertThat(!bpmnModel.getFlowLocationMap().isEmpty()).isTrue();

    // Test the flow
    org.activiti.bpmn.model.Process process = bpmnModel.getProcesses().get(0);
    List<StartEvent> startEvents = process.findFlowElementsOfType(StartEvent.class);
    assertThat(startEvents).hasSize(1);
    StartEvent startEvent = startEvents.get(0);
    assertThat(startEvent.getOutgoingFlows()).hasSize(1);
    assertThat(startEvent.getIncomingFlows()).hasSize(0);

    String nextElementId = startEvent.getOutgoingFlows().get(0).getTargetRef();
    UserTask userTask = (UserTask) process.getFlowElement(nextElementId);
    assertThat(userTask.getName()).isEqualTo("First Task");

    assertThat(userTask.getOutgoingFlows()).hasSize(1);
    assertThat(userTask.getIncomingFlows()).hasSize(1);
    nextElementId = userTask.getOutgoingFlows().get(0).getTargetRef();
    ParallelGateway parallelGateway = (ParallelGateway) process.getFlowElement(nextElementId);
    assertThat(parallelGateway.getOutgoingFlows()).hasSize(2);

    nextElementId = parallelGateway.getOutgoingFlows().get(0).getTargetRef();
    assertThat(parallelGateway.getIncomingFlows()).hasSize(1);
    userTask = (UserTask) process.getFlowElement(nextElementId);
    assertThat(userTask.getOutgoingFlows()).hasSize(1);

    nextElementId = userTask.getOutgoingFlows().get(0).getTargetRef();
    parallelGateway = (ParallelGateway) process.getFlowElement(nextElementId);
    assertThat(parallelGateway.getOutgoingFlows()).hasSize(1);
    assertThat(parallelGateway.getIncomingFlows()).hasSize(2);

    nextElementId = parallelGateway.getOutgoingFlows().get(0).getTargetRef();
    EndEvent endEvent = (EndEvent) process.getFlowElement(nextElementId);
    assertThat(endEvent.getOutgoingFlows()).hasSize(0);
    assertThat(endEvent.getIncomingFlows()).hasSize(1);
  }

  /**
   * This test was added due to issues with unzip of JDK 7, where the default is changed to UTF8 instead of the platform encoding (which is, in fact, good). However, some platforms do not create
   * UTF8-compatible ZIP files.
   *
   * The tested zip file is created on OS X (non-UTF-8).
   *
   * See https://blogs.oracle.com/xuemingshen/entry/non_utf_8_encoding_in
   */
  public void testDeployZipFile() {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("org/activiti/engine/test/api/repository/test-processes.zip");
    assertThat(inputStream).isNotNull();
    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
    assertThat(zipInputStream).isNotNull();
    repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();

    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(6);

    // Delete
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

}
