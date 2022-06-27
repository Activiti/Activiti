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

import java.nio.charset.StandardCharsets;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EncodingConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFileEncoding(StandardCharsets.ISO_8859_1.name());
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFileEncoding(StandardCharsets.ISO_8859_1.name());
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("writeReportTask");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(UserTask.class);
    assertThat(flowElement.getId()).isEqualTo("writeReportTask");
    UserTask userTask = (UserTask) flowElement;
    assertThat(userTask.getId()).isEqualTo("writeReportTask");
    assertThat(userTask.getName()).isEqualTo("Fazer relatório");
  }

  protected String getResource() {
    return "encoding.bpmn";
  }
}
