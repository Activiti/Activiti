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
import org.activiti.bpmn.model.ScriptTask;
import org.junit.jupiter.api.Test;

public class TextAnnotationConverterTest extends AbstractConverterTest {

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
    return "parsing_error_on_extension_elements.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getFlowElement("_5");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(ScriptTask.class);
    assertThat(flowElement.getId()).isEqualTo("_5");
    ScriptTask scriptTask = (ScriptTask) flowElement;
    assertThat(scriptTask.getId()).isEqualTo("_5");
    assertThat(scriptTask.getName()).isEqualTo("Send Hello Message");
  }
}
