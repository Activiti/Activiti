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
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

public class MultiInstanceConverterTest extends AbstractConverterTest {

    @Test
    public void shouldConvertXMLToModel() throws Exception {
        BpmnModel bpmnModel = readXMLFile();
        validateModel(bpmnModel);
    }

    @Test
    public void shouldConvertModelToXML() throws Exception {
        BpmnModel bpmnModel = readXMLFile();
        BpmnModel exportedModel = exportAndReadXMLFile(bpmnModel);
        validateModel(exportedModel);
    }

    private void validateModel(BpmnModel bpmnModel) {
        FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement("miTasks");
        assertThat(flowElement)
            .isNotNull()
            .isInstanceOf(UserTask.class);
        MultiInstanceLoopCharacteristics loopCharacteristics = ((UserTask) flowElement)
            .getLoopCharacteristics();
        assertThat(loopCharacteristics)
            .extracting(MultiInstanceLoopCharacteristics::getLoopDataOutputRef,
                MultiInstanceLoopCharacteristics::getOutputDataItem)
            .containsExactly(
                "meals", "meal");
    }

    @Override
    protected String getResource() {
        return "multi-instance-sequence-output-data-ref.bpmn20.xml";
    }
}
