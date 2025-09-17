/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MultiInstanceExportTest {

    @Mock
    private XMLStreamWriter xtw;

    @Test
    public void shouldWriteStartElementWhenMultiInstanceLoopCharacteristicsIsNotNull() throws Exception {
        Activity activity = mock(Activity.class);
        MultiInstanceLoopCharacteristics multiInstance = new MultiInstanceLoopCharacteristics();
        when(activity.getLoopCharacteristics()).thenReturn(multiInstance);

        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter xtw = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);

        MultiInstanceExport.writeMultiInstance(activity, xtw);

        String generatedXml = stringWriter.toString();

        assertTrue(
            generatedXml.contains(
                "<bpmn2:multiInstanceLoopCharacteristics isSequential=\"false\"></bpmn2:multiInstanceLoopCharacteristics>"
            )
        );
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

        assertTrue(
            generatedXml.contains(
                "<bpmn2:multiInstanceLoopCharacteristics isSequential=\"true\"><bpmn2:loopCardinality>100</bpmn2:loopCardinality></bpmn2:multiInstanceLoopCharacteristics>"
            )
        );
    }

    @Test
    public void shouldNotWriteStartElementWhenMultiInstanceLoopCharacteristicsIsNull() throws Exception {
        Activity activity = mock(Activity.class);
        when(activity.getLoopCharacteristics()).thenReturn(null);

        MultiInstanceExport.writeMultiInstance(activity, xtw);
        verify(xtw, never()).writeStartElement(anyString());
    }

    @Test
    public void shouldWriteMultiInstanceLoopCharacteristicsWithAllFieldsPopulated() throws Exception {
        Activity activity = mock(Activity.class);
        MultiInstanceLoopCharacteristics multiInstance = mock(MultiInstanceLoopCharacteristics.class);
        when(activity.getLoopCharacteristics()).thenReturn(multiInstance);
        when(multiInstance.isSequential()).thenReturn(true);
        when(multiInstance.getInputDataItem()).thenReturn("inputCollection");
        when(multiInstance.getElementVariable()).thenReturn("item");
        when(multiInstance.getElementIndexVariable()).thenReturn("index");
        when(multiInstance.getLoopCardinality()).thenReturn("10");
        when(multiInstance.getLoopDataOutputRef()).thenReturn("outputCollection");
        when(multiInstance.getOutputDataItem()).thenReturn("outputItem");
        when(multiInstance.getCompletionCondition()).thenReturn("${condition}");

        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter xtw = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
        MultiInstanceExport.writeMultiInstance(activity, xtw);

        String generatedXml = stringWriter.toString();

        assertThat(generatedXml).contains(
            "<bpmn2:multiInstanceLoopCharacteristics isSequential=\"true\"",
            "activiti:collection=\"inputCollection\"",
            "activiti:elementVariable=\"item\"",
            "activiti:elementIndexVariable=\"index\"",
            "<bpmn2:loopCardinality>10</bpmn2:loopCardinality>",
            "<bpmn2:loopDataOutputRef>outputCollection</bpmn2:loopDataOutputRef>",
            "<bpmn2:outputDataItem name=\"outputItem\"></bpmn2:outputDataItem>",
            "<bpmn2:completionCondition>${condition}</bpmn2:completionCondition>",
            "</bpmn2:multiInstanceLoopCharacteristics>"
        );
    }
}
