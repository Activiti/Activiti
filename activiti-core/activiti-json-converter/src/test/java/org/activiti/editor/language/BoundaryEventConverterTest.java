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

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.junit.jupiter.api.Test;

public class BoundaryEventConverterTest extends AbstractConverterTest {

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

  @Override
  protected String getResource() {
    return "test.boundaryeventmodel.json";
  }

  private void validateModel(BpmnModel model) {

    BoundaryEvent errorElement = (BoundaryEvent) model.getMainProcess().getFlowElement("errorEvent", true);
    ErrorEventDefinition errorEvent = (ErrorEventDefinition) extractEventDefinition(errorElement);
    assertThat(errorElement.isCancelActivity()).isTrue(); // always true
    assertThat(errorEvent.getErrorRef()).isEqualTo("errorRef");
    assertThat(errorElement.getAttachedToRefId()).isEqualTo("sid-F21E9F4D-EA19-44DF-B1D3-14663A809CAE");

    BoundaryEvent signalElement = (BoundaryEvent) model.getMainProcess().getFlowElement("signalEvent", true);
    SignalEventDefinition signalEvent = (SignalEventDefinition) extractEventDefinition(signalElement);
    assertThat(signalElement.isCancelActivity()).isFalse();
    assertThat(signalEvent.getSignalRef()).isEqualTo("signalRef");
    assertThat(errorElement.getAttachedToRefId()).isEqualTo("sid-F21E9F4D-EA19-44DF-B1D3-14663A809CAE");

    BoundaryEvent messageElement = (BoundaryEvent) model.getMainProcess().getFlowElement("messageEvent", true);
    MessageEventDefinition messageEvent = (MessageEventDefinition) extractEventDefinition(messageElement);
    assertThat(messageElement.isCancelActivity()).isFalse();
    assertThat(messageEvent.getMessageRef()).isEqualTo("messageRef");
    assertThat(errorElement.getAttachedToRefId()).isEqualTo("sid-F21E9F4D-EA19-44DF-B1D3-14663A809CAE");

    BoundaryEvent timerElement = (BoundaryEvent) model.getMainProcess().getFlowElement("timerEvent", true);
    TimerEventDefinition timerEvent = (TimerEventDefinition) extractEventDefinition(timerElement);
    assertThat(timerElement.isCancelActivity()).isFalse();
    assertThat(timerEvent.getTimeDuration()).isEqualTo("PT5M");
    assertThat(errorElement.getAttachedToRefId()).isEqualTo("sid-F21E9F4D-EA19-44DF-B1D3-14663A809CAE");

  }

}
