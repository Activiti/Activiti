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
package org.activiti.bpmn.converter.child;

import static org.activiti.bpmn.constants.BpmnXMLConstants.ELEMENT_MULTIINSTANCE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.activiti.bpmn.converter.child.multi.instance.MultiInstanceParser;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.UserTask;
import org.junit.jupiter.api.Test;

public class MultiInstanceParserTest {

    private MultiInstanceParser parser = new MultiInstanceParser();
    private XMLInputFactory xif = XMLInputFactory.newInstance();

    @Test
    public void parseChildElement_should_setLoopCharacteristicsProperties() throws Exception {
        try (InputStream xmlStream = this.getClass().getClassLoader()
            .getResourceAsStream("multi-instance-loop-characteristics.xml")) {
            XMLStreamReader xtr = xif.createXMLStreamReader(xmlStream, "UTF-8");
            xtr.next();

            UserTask userTask = new UserTask();
            parser.parseChildElement(xtr, userTask, null);

            MultiInstanceLoopCharacteristics loopCharacteristics = userTask
                .getLoopCharacteristics();

            assertThat(loopCharacteristics).isNotNull();
            assertThat(loopCharacteristics.isSequential()).isFalse();
            assertThat(loopCharacteristics.getInputDataItem()).isEqualTo("assigneeList");
            assertThat(loopCharacteristics.getElementVariable()).isEqualTo("assignee");
            assertThat(loopCharacteristics.getCompletionCondition()).isEqualTo("${nrOfCompletedInstances/nrOfInstances >= 0.6 }");
            assertThat(loopCharacteristics.getLoopDataOutputRef()).isEqualTo("meals");
            assertThat(loopCharacteristics.getOutputDataItem()).isEqualTo("meal");
        }

    }

    @Test
    public void parseChildElement_should_setActivitiExtensionsElements() throws Exception {
        try (InputStream xmlStream = this.getClass().getClassLoader()
            .getResourceAsStream("multi-instance-loop-characteristics-extensions.xml")) {
            XMLStreamReader xtr = xif.createXMLStreamReader(xmlStream, "UTF-8");
            moveReaderToMultiInstanceLine(xtr);

            UserTask userTask = new UserTask();
            parser.parseChildElement(xtr, userTask, null);

            MultiInstanceLoopCharacteristics loopCharacteristics = userTask
                .getLoopCharacteristics();

            assertThat(loopCharacteristics).isNotNull();
            assertThat(loopCharacteristics.isSequential()).isTrue();
            assertThat(loopCharacteristics.getInputDataItem()).isEqualTo("assigneeList");
            assertThat(loopCharacteristics.getElementVariable()).isEqualTo("assignee");
            assertThat(loopCharacteristics.getElementIndexVariable()).isEqualTo("loopValueIndex");
        }

    }

    private void moveReaderToMultiInstanceLine(XMLStreamReader xtr) throws XMLStreamException {
        do {
            xtr.next();
        } while (!isMultiInstance(xtr) && xtr.hasNext());
    }

    private boolean isMultiInstance(XMLStreamReader xtr) {
        return xtr.isStartElement() && xtr.getLocalName().equals(ELEMENT_MULTIINSTANCE);
    }
}
