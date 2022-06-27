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
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.junit.jupiter.api.Test;

public class EndEventConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(exportAndReadXMLFile(bpmnModel));
  }

  @Override
  protected String getResource() {
    return "end-error-event.bpmn20.xml";
  }

  private void validateModel(BpmnModel model) {
    assertThat(model.getDefinitionsAttributes()).hasSize(2);

    FlowElement flowElement = model.getMainProcess().getFlowElement("EndEvent_0mdpjzn");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(EndEvent.class);

    EndEvent endEvent = (EndEvent) flowElement;

    assertThat(endEvent.getIncomingFlows()).hasSize(1);
    assertThat(endEvent.getEventDefinitions()).hasSize(1);

    //Check that incoming xml element is coming before error event definition
    assertThat(endEvent.getIncomingFlows().get(0).getXmlRowNumber()).isLessThan(endEvent.getEventDefinitions().get(0).getXmlRowNumber());

    assertThat(endEvent.getEventDefinitions().get(0)).isInstanceOf(ErrorEventDefinition.class);
    ErrorEventDefinition errorEventDefinition = (ErrorEventDefinition) endEvent.getEventDefinitions().get(0);

    assertThat(errorEventDefinition.getErrorRef()).isEqualTo("Error_01agmko");
  }
}
