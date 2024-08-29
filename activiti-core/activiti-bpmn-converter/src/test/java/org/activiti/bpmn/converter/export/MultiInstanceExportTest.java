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
package org.activiti.bpmn.converter.export;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MultiInstanceExportTest {

    @Mock
    private XMLStreamWriter xtw;
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldWriteStartElementWhenMultiInstanceLoopCharacteristicsIsNotNull() throws Exception {

        Activity activity = mock(Activity.class);
        MultiInstanceLoopCharacteristics multiInstance = new MultiInstanceLoopCharacteristics();
        when(activity.getLoopCharacteristics()).thenReturn(multiInstance);

        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter xtw = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);

        MultiInstanceExport.writeMultiInstance(activity, xtw);

        String generatedXml = stringWriter.toString();


        assertTrue(generatedXml.contains("<multiInstanceLoopCharacteristics></multiInstanceLoopCharacteristics>"));

    }
    @Test
    public void shouldWriteStartElementWhenMultiInstanceLoopCharacteristicsHasCardinality() throws Exception {

        Activity activity = mock(Activity.class);
        MultiInstanceLoopCharacteristics multiInstance = new MultiInstanceLoopCharacteristics();
        multiInstance.setSequential(true);
        multiInstance.setLoopCardinality("100");
        when(activity.getLoopCharacteristics()).thenReturn(multiInstance);


        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter xtw = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);

        MultiInstanceExport.writeMultiInstance(activity, xtw);

        String generatedXml = stringWriter.toString();


        assertTrue(generatedXml.contains("<multiInstanceLoopCharacteristics isSequential=\"true\"><loopCardinality>100</loopCardinality></multiInstanceLoopCharacteristics>"));
    }
    @Test
    public void shouldNotWriteStartElementWhenMultiInstanceLoopCharacteristicsIsNull() throws Exception {

        Activity activity = mock(Activity.class);
        when(activity.getLoopCharacteristics()).thenReturn(null);

        MultiInstanceExport.writeMultiInstance(activity, xtw);
        verify(xtw, never()).writeStartElement(anyString());
    }
}
