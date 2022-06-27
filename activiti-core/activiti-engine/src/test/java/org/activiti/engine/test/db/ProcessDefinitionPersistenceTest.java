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


package org.activiti.engine.test.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;

/**


 */
public class ProcessDefinitionPersistenceTest extends PluggableActivitiTestCase {

  public void testProcessDefinitionPersistence() {
    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/db/processOne.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/db/processTwo.bpmn20.xml").deploy().getId();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertThat(processDefinitions).hasSize(2);

    repositoryService.deleteDeployment(deploymentId);
  }

  public void testProcessDefinitionIntrospection() {
    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/db/processOne.bpmn20.xml").deploy().getId();

    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    ProcessDefinition processDefinition = ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(procDefId);

    assertThat(processDefinition.getId()).isEqualTo(procDefId);
    assertThat(processDefinition.getName()).isEqualTo("Process One");

    Process process = repositoryService.getBpmnModel(processDefinition.getId()).getMainProcess();
    StartEvent startElement = (StartEvent) process.getFlowElement("start");
    assertThat(startElement).isNotNull();
    assertThat(startElement.getId()).isEqualTo("start");
    assertThat(startElement.getName()).isEqualTo("S t a r t");
    assertThat(startElement.getDocumentation()).isEqualTo("the start event");
    List<SequenceFlow> outgoingFlows = startElement.getOutgoingFlows();
    assertThat(outgoingFlows).hasSize(1);
    assertThat(outgoingFlows.get(0).getConditionExpression()).isEqualTo("${a == b}");

    EndEvent endElement = (EndEvent) process.getFlowElement("end");
    assertThat(endElement).isNotNull();
    assertThat(endElement.getId()).isEqualTo("end");

    assertThat(outgoingFlows.get(0).getId()).isEqualTo("flow1");
    assertThat(outgoingFlows.get(0).getName()).isEqualTo("Flow One");
    assertThat(outgoingFlows.get(0).getDocumentation()).isEqualTo("The only transitions in the process");
    assertThat(outgoingFlows.get(0).getSourceFlowElement()).isSameAs(startElement);
    assertThat(outgoingFlows.get(0).getTargetFlowElement()).isSameAs(endElement);

    repositoryService.deleteDeployment(deploymentId);
  }

  public void testProcessDefinitionQuery() {
    String deployment1Id = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/db/processOne.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/db/processTwo.bpmn20.xml").deploy().getId();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionName().asc().orderByProcessDefinitionVersion().asc().list();

    assertThat(processDefinitions).hasSize(2);

    String deployment2Id = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/db/processOne.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/db/processTwo.bpmn20.xml").deploy().getId();

    assertThat(repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionName().asc().count()).isEqualTo(4);
    assertThat(repositoryService.createProcessDefinitionQuery().latestVersion().orderByProcessDefinitionName().asc().count()).isEqualTo(2);

    repositoryService.deleteDeployment(deployment1Id);
    repositoryService.deleteDeployment(deployment2Id);
  }

  public void testProcessDefinitionGraphicalNotationFlag() {
    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/db/process-with-di.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/db/process-without-di.bpmn20.xml").deploy().getId();

    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(2);

    ProcessDefinition processWithDi = repositoryService.createProcessDefinitionQuery().processDefinitionKey("processWithDi").singleResult();
    assertThat(processWithDi.hasGraphicalNotation()).isTrue();

    ProcessDefinition processWithoutDi = repositoryService.createProcessDefinitionQuery().processDefinitionKey("processWithoutDi").singleResult();
    assertThat(processWithoutDi.hasGraphicalNotation()).isFalse();

    repositoryService.deleteDeployment(deploymentId);

  }

}
