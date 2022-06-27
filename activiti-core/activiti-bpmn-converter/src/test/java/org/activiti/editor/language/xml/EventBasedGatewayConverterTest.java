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

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.junit.jupiter.api.Test;

/**
 * Test for ACT-1657
 *

 */
public class EventBasedGatewayConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  protected String getResource() {
    return "eventgatewaymodel.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("eventBasedGateway");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(EventGateway.class);

    EventGateway gateway = (EventGateway) flowElement;
    List<ActivitiListener> listeners = gateway.getExecutionListeners();
    assertThat(listeners).hasSize(1);
    ActivitiListener listener = listeners.get(0);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("org.test.TestClass");
    assertThat(listener.getEvent()).isEqualTo("start");
  }
}
