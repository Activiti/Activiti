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
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

public class CompleteConverterTest extends AbstractConverterTest {

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
    return "completemodel.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("userTask1");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(UserTask.class);
    assertThat(flowElement.getId()).isEqualTo("userTask1");

    flowElement = model.getMainProcess().getFlowElement("catchsignal");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(IntermediateCatchEvent.class);
    assertThat(flowElement.getId()).isEqualTo("catchsignal");
    IntermediateCatchEvent catchEvent = (IntermediateCatchEvent) flowElement;
    assertThat(catchEvent.getEventDefinitions()).hasSize(1);
    assertThat(catchEvent.getEventDefinitions().get(0)).isInstanceOf(SignalEventDefinition.class);
    SignalEventDefinition signalEvent = (SignalEventDefinition) catchEvent.getEventDefinitions().get(0);
    assertThat(signalEvent.getSignalRef()).isEqualTo("testSignal");

    flowElement = model.getMainProcess().getFlowElement("subprocess");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(SubProcess.class);
    assertThat(flowElement.getId()).isEqualTo("subprocess");
    SubProcess subProcess = (SubProcess) flowElement;

    flowElement = subProcess.getFlowElement("receiveTask");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(ReceiveTask.class);
    assertThat(flowElement.getId()).isEqualTo("receiveTask");
  }
}
