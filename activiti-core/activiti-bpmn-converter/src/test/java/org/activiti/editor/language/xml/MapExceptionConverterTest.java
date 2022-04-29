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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ServiceTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MapExceptionConverterTest extends AbstractConverterTest {

    String resourceName;

    protected String getResource() {
        return resourceName;
    }

    @Test
    public void testMapExceptionWithInvalidHasChildren() throws Exception {
        resourceName = "mapException/mapExceptionInvalidHasChildrenModel.bpmn";
        assertThatExceptionOfType(XMLException.class)
            .as("No exception is thrown for mapExecution with invalid boolean for hasChildren")
            .isThrownBy(() -> readXMLFile())
            .withMessageContaining("is not valid boolean");
    }

    @Test
    public void testMapExceptionWithNoErrorCode() throws Exception {
        resourceName = "mapException/mapExceptionNoErrorCode.bpmn";
        assertThatExceptionOfType(XMLException.class)
            .as("No exception is thrown for mapExecution with no Error Code")
            .isThrownBy(() -> readXMLFile())
            .withMessageContaining("No errorCode defined");
    }

    @Test
    public void testMapExceptionWithNoExceptionClass() throws Exception {
        resourceName = "mapException/mapExceptionNoExceptionClass.bpmn";

        BpmnModel bpmnModel = readXMLFile();
        FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement("servicetaskWithAndTrueAndChildren");
        assertThat(flowElement).isNotNull();
        assertThat(flowElement).isInstanceOf(ServiceTask.class);
        assertThat(flowElement.getId()).isEqualTo("servicetaskWithAndTrueAndChildren");
        ServiceTask serviceTask = (ServiceTask) flowElement;
        assertThat(serviceTask.getMapExceptions()).isNotNull();
        assertThat(serviceTask.getMapExceptions()).hasSize(1);
        assertThat(serviceTask.getMapExceptions().get(0).getClassName()).isNotNull();
        assertThat(serviceTask.getMapExceptions().get(0).getClassName().length()).isEqualTo(0);
    }

    @Test
    public void convertXMLToModel() throws Exception {
        resourceName = "mapException/mapExceptionModel.bpmn";

        BpmnModel bpmnModel = readXMLFile();
        validateModel(bpmnModel);
    }

    private void validateModel(BpmnModel model) {

        // check service task with andChildren Set to True
        FlowElement flowElement = model.getMainProcess().getFlowElement("servicetaskWithAndTrueAndChildren");
        assertThat(flowElement).isNotNull();
        assertThat(flowElement).isInstanceOf(ServiceTask.class);
        assertThat(flowElement.getId()).isEqualTo("servicetaskWithAndTrueAndChildren");
        ServiceTask serviceTask = (ServiceTask) flowElement;
        assertThat(serviceTask.getMapExceptions()).isNotNull();
        assertThat(serviceTask.getMapExceptions()).hasSize(3);

        // check a normal mapException, with hasChildren == true
        assertThat(serviceTask.getMapExceptions().get(0).getErrorCode()).isEqualTo("myErrorCode1");
        assertThat(serviceTask.getMapExceptions().get(0).getClassName()).isEqualTo("com.activiti.Something1");
        assertThat(serviceTask.getMapExceptions().get(0).isAndChildren()).isTrue();

        // check a normal mapException, with hasChildren == false
        assertThat(serviceTask.getMapExceptions().get(1).getErrorCode()).isEqualTo("myErrorCode2");
        assertThat(serviceTask.getMapExceptions().get(1).getClassName()).isEqualTo("com.activiti.Something2");
        assertThat(serviceTask.getMapExceptions().get(1).isAndChildren()).isFalse();

        // check a normal mapException, with no hasChildren Defined, default should be false
        assertThat(serviceTask.getMapExceptions().get(2).getErrorCode()).isEqualTo("myErrorCode3");
        assertThat(serviceTask.getMapExceptions().get(2).getClassName()).isEqualTo("com.activiti.Something3");
        assertThat(serviceTask.getMapExceptions().get(2).isAndChildren()).isFalse();

        // if no map exception is defined, getMapException should return a not null empty list
        FlowElement flowElement1 = model.getMainProcess().getFlowElement("servicetaskWithNoMapException");
        assertThat(flowElement1).isNotNull();
        assertThat(flowElement1).isInstanceOf(ServiceTask.class);
        assertThat(flowElement1.getId()).isEqualTo("servicetaskWithNoMapException");
        ServiceTask serviceTask1 = (ServiceTask) flowElement1;
        assertThat(serviceTask1.getMapExceptions()).isNotNull();
        assertThat(serviceTask1.getMapExceptions()).hasSize(0);
    }
}
