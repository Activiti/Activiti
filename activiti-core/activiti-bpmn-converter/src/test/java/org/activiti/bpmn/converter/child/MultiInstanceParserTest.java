/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.bpmn.converter.child;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.activiti.bpmn.converter.child.multi.instance.MultiInstanceParser;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class MultiInstanceParserTest {

    private MultiInstanceParser multiInstanceParser = new MultiInstanceParser();
    private XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    @Test
    public void parseChildElement_should_setLoopCharacteristicsProperties() throws Exception {
        MultiInstanceLoopCharacteristics loopCharacteristics = parseLoopCharacteristics(
            "multi-instance-loop-characteristics.xml");

        assertThat(loopCharacteristics).isNotNull();
        assertThat(loopCharacteristics.isSequential()).isFalse();
        assertThat(loopCharacteristics.getInputDataItem()).isEqualTo("assigneeList");
        assertThat(loopCharacteristics.getElementVariable()).isEqualTo("assignee");
        assertThat(loopCharacteristics.getCompletionCondition())
            .isEqualTo("${nrOfCompletedInstances/nrOfInstances >= 0.6 }");
        assertThat(loopCharacteristics.getLoopDataOutputRef()).isEqualTo("meals");
        assertThat(loopCharacteristics.getOutputDataItem()).isEqualTo("meal");

    }

    private MultiInstanceLoopCharacteristics parseLoopCharacteristics(String resourceName)
        throws Exception {
        MultiInstanceLoopCharacteristics loopCharacteristics;
        try (InputStream xmlStream = this.getClass().getClassLoader()
            .getResourceAsStream(resourceName)) {
            XMLStreamReader xtr = xmlInputFactory.createXMLStreamReader(xmlStream, "UTF-8");
            xtr.next();

            UserTask userTask = new UserTask();
            multiInstanceParser.parseChildElement(xtr, userTask, null);

            loopCharacteristics = userTask
                .getLoopCharacteristics();
        }
        return loopCharacteristics;
    }

    @Test
    public void parseChildElement_should_setActivitiExtensionsElements() throws Exception {
        MultiInstanceLoopCharacteristics loopCharacteristics = parseLoopCharacteristics(
            "multi-instance-loop-characteristics-extensions.xml");

        assertThat(loopCharacteristics).isNotNull();
        assertThat(loopCharacteristics.isSequential()).isTrue();
        assertThat(loopCharacteristics.getInputDataItem()).isEqualTo("assigneeList");
        assertThat(loopCharacteristics.getElementVariable()).isEqualTo("assignee");
        assertThat(loopCharacteristics.getElementIndexVariable()).isEqualTo("loopValueIndex");
    }

    @Test
    public void parseChildElement_should_readDataOutputItemWhenValueIsSetAsElementText()
        throws Exception {
        MultiInstanceLoopCharacteristics loopCharacteristics = parseLoopCharacteristics(
            "multi-instance-loop-characteristics-output-data-item-as-value.xml");

        assertThat(loopCharacteristics).isNotNull();
        assertThat(loopCharacteristics.getOutputDataItem()).isEqualTo("meal");
    }

}
