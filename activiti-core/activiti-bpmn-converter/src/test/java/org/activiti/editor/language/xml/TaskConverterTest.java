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

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Task;
import org.junit.jupiter.api.Test;

public class TaskConverterTest extends AbstractConverterTest {

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

    @Override
    protected String getResource() {
        return "taskModel.bpmn";
    }

    private void validateModel(BpmnModel model) throws Exception {
        FlowElement flowElement = model.getMainProcess().getFlowElement("Task_17unmd8");
        assertThat(flowElement).isNotNull();
        assertThat(flowElement).isInstanceOf(Task.class);

        checkXml(model);
    }

    private void checkXml(BpmnModel model) throws Exception {

        String xml = new String(new BpmnXMLConverter().convertToXML(model),
                                "UTF-8");

        assertThat(xml).contains("incoming>SequenceFlow_12r6kuh<",
                                 "outgoing>SequenceFlow_1h7t0iw<");

    }
}
