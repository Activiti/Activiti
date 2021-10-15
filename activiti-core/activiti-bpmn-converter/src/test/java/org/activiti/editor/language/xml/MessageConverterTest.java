/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
import org.activiti.bpmn.model.Message;
import org.junit.jupiter.api.Test;

public class MessageConverterTest extends AbstractConverterTest {

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
    }

    private void validateModel(BpmnModel model) {
        Message message = model.getMessage("writeReport");
        assertThat(message).isNotNull();
        assertThat(message.getItemRef()).isEqualTo("Examples:writeReportItem");
        assertThat(message.getName()).isEqualTo("newWriteReport");
        assertThat(message.getId()).isEqualTo("writeReport");

        Message message2 = model.getMessage("writeReport2");
        assertThat(message2).isNotNull();
        assertThat(message2.getItemRef())
            .isEqualTo("http://foo.bar.com/Examples:writeReportItem2");
        assertThat(message2.getName()).isEqualTo("newWriteReport2");
        assertThat(message2.getId()).isEqualTo("writeReport2");

        Message message3 = model.getMessage("writeReport3");
        assertThat(message3).isNotNull();
        assertThat(message3.getItemRef())
            .isEqualTo("Examples:writeReportItem3");
        assertThat(message3.getName()).isEqualTo("newWriteReport3");
        assertThat(message3.getId()).isEqualTo("writeReport3");
    }

    protected String getResource() {
        return "message.bpmn";
    }
}
