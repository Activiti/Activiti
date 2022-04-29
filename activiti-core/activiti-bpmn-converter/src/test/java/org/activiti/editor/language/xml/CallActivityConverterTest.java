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

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IOParameter;
import org.junit.jupiter.api.Test;

public class CallActivityConverterTest extends AbstractConverterTest {

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
    return "callactivity.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("callactivity");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(CallActivity.class);
    CallActivity callActivity = (CallActivity) flowElement;
    assertThat(callActivity.getId()).isEqualTo("callactivity");
    assertThat(callActivity.getName()).isEqualTo("Call activity");

    assertThat(callActivity.getCalledElement()).isEqualTo("processId");

    List<IOParameter> parameters = callActivity.getInParameters();
    assertThat(parameters).hasSize(2);
    IOParameter parameter = parameters.get(0);
    assertThat(parameter.getSource()).isEqualTo("test");
    assertThat(parameter.getTarget()).isEqualTo("test");
    parameter = parameters.get(1);
    assertThat(parameter.getSourceExpression()).isEqualTo("${test}");
    assertThat(parameter.getTarget()).isEqualTo("test");

    parameters = callActivity.getOutParameters();
    assertThat(parameters).hasSize(1);
    parameter = parameters.get(0);
    assertThat(parameter.getSource()).isEqualTo("test");
    assertThat(parameter.getTarget()).isEqualTo("test");
  }
}
