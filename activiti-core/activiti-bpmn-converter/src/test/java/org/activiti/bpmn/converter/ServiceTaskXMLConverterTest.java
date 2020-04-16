/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.bpmn.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ServiceTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.activiti.bpmn.constants.BpmnXMLConstants.ATTRIBUTE_TASK_IMPLEMENTATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ServiceTaskXMLConverterTest {

    private ServiceTaskXMLConverter converter = new ServiceTaskXMLConverter();

    @Mock(answer = Answers.RETURNS_MOCKS)
    private XMLStreamReader reader;

    @Spy
    private XMLStreamWriter writer;

    @Mock
    private BpmnModel bpmnModel;

    @Mock
    private ServiceTask serviceTask;

    @BeforeEach
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void convertXMLToElementShouldSetTheImplementationFromXMLImplementationAttribute() throws Exception {
        //given
        given(reader.getAttributeValue(null,
                                       BpmnXMLConstants.ATTRIBUTE_TASK_IMPLEMENTATION)).willReturn("myConnector");

        //when
        BaseElement element = converter.convertXMLToElement(reader,
                                                            new BpmnModel());

        //then
        assertThat(((ServiceTask) element).getImplementation()).isEqualTo("myConnector");
    }

    @Test
    public void convertServiceTaskElementToXMLShouldWriteTheImplementionAttribute() throws Exception {
        //given
        given(serviceTask.getImplementation()).willReturn("myConnectorImplementation");

        //when
        converter.writeAdditionalAttributes(serviceTask,
                                            bpmnModel,
                                            writer);

        //then
        verify(writer).writeAttribute(eq(ATTRIBUTE_TASK_IMPLEMENTATION),
                                      eq("myConnectorImplementation"));
    }
}
