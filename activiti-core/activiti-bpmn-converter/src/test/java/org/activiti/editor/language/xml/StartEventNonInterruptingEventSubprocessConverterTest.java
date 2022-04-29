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
import static org.assertj.core.api.Assertions.tuple;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.StartEvent;
import org.junit.jupiter.api.Test;

public class StartEventNonInterruptingEventSubprocessConverterTest extends AbstractConverterTest {

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
        Message message = model.getMessage("Message_1");

        assertThat(message).isNotNull()
                           .extracting(Message::getId,
                                       Message::getName)
                           .contains("Message_1",
                                     "eventSubprocessMessage");

        assertThat(model.getProcessById("process")
                        .getFlowElements()).filteredOn(EventSubProcess.class::isInstance)
                                           .flatExtracting("flowElements")
                                           .filteredOn(StartEvent.class::isInstance)
                                           .extracting("id", "isInterrupting")
                                           .contains(tuple("eventProcessStart", false));

    }

    protected String getResource() {
        return "StartEventNonInterruptingEventSubprocessConverterTest.bpmn";
    }
}
