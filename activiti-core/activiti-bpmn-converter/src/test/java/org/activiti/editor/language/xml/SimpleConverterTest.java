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
package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

public class SimpleConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
    deployProcess(parsedModel);
  }

  protected String getResource() {
    return "simplemodel.bpmn";
  }

  private void validateModel(BpmnModel model) {
    assertThat(model.getDefinitionsAttributes()).hasSize(2);
    assertThat(model.getDefinitionsAttributeValue("http://activiti.com/modeler", "version")).isEqualTo("2.2A");
    assertThat(model.getDefinitionsAttributeValue("http://activiti.com/modeler", "exportDate")).isEqualTo("20140312T10:45:23");

    assertThat(model.getMainProcess().getId()).isEqualTo("simpleProcess");
    assertThat(model.getMainProcess().getName()).isEqualTo("Simple process");
    assertThat(model.getMainProcess().getDocumentation()).isEqualTo("simple doc");
    assertThat(model.getMainProcess().isExecutable()).isEqualTo(true);

    FlowElement flowElement = model.getMainProcess().getFlowElement("flow1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SequenceFlow.class);
    assertThat(flowElement.getId()).isEqualTo("flow1");

    flowElement = model.getMainProcess().getFlowElement("catchEvent");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(IntermediateCatchEvent.class);
    assertThat(flowElement.getId()).isEqualTo("catchEvent");
    IntermediateCatchEvent catchEvent = (IntermediateCatchEvent) flowElement;
    assertThat(catchEvent.getEventDefinitions().size() == 1).isTrue();
    EventDefinition eventDefinition = catchEvent.getEventDefinitions().get(0);
    assertThat(eventDefinition).isInstanceOf(TimerEventDefinition.class);
    TimerEventDefinition timerDefinition = (TimerEventDefinition) eventDefinition;
    assertThat(timerDefinition.getTimeDuration()).isEqualTo("PT5M");

    flowElement = model.getMainProcess().getFlowElement("userTask1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(UserTask.class);
    UserTask task = (UserTask) flowElement;
    assertThat(task.getDocumentation()).isEqualTo("task doc");

    flowElement = model.getMainProcess().getFlowElement("flow1Condition");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SequenceFlow.class);
    assertThat(flowElement.getId()).isEqualTo("flow1Condition");
    SequenceFlow flow = (SequenceFlow) flowElement;
    assertThat(flow.getConditionExpression()).isEqualTo("${number <= 1}");

    flowElement = model.getMainProcess().getFlowElement("gateway1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(ExclusiveGateway.class);
  }
}
