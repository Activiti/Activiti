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
package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.junit.jupiter.api.Test;

public class SimpleConverterTest extends AbstractConverterTest {

  @Test
  public void convertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  protected String getResource() {
    return "test.simplemodel.json";
  }

  private void validateModel(BpmnModel model) {
    assertThat(model.getMainProcess().getId()).isEqualTo("simpleProcess");
    assertThat(model.getMainProcess().getName()).isEqualTo("Simple process");
    assertThat(model.getMainProcess().isExecutable()).isEqualTo(true);

    FlowElement flowElement = model.getMainProcess().getFlowElement("flow1", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SequenceFlow.class);
    assertThat(flowElement.getId()).isEqualTo("flow1");

    flowElement = model.getMainProcess().getFlowElement("catchEvent", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(IntermediateCatchEvent.class);
    assertThat(flowElement.getId()).isEqualTo("catchEvent");
    IntermediateCatchEvent catchEvent = (IntermediateCatchEvent) flowElement;
    assertThat(catchEvent.getEventDefinitions().size() == 1).isTrue();
    EventDefinition eventDefinition = catchEvent.getEventDefinitions().get(0);
    assertThat(eventDefinition).isInstanceOf(TimerEventDefinition.class);
    TimerEventDefinition timerDefinition = (TimerEventDefinition) eventDefinition;
    assertThat(timerDefinition.getTimeDuration()).isEqualTo("PT5M");

    flowElement = model.getMainProcess().getFlowElement("flow1Condition", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SequenceFlow.class);
    assertThat(flowElement.getId()).isEqualTo("flow1Condition");
    SequenceFlow flow = (SequenceFlow) flowElement;
    assertThat(flow.getConditionExpression()).isEqualTo("${number <= 1}");
  }
}
