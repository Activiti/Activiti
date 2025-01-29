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
package org.activiti.bpmn.converter;

import org.activiti.bpmn.model.LinkEventDefinition;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LinkEventDefinitionXMLConverterTest {

    @Test
    public void should_writeLinkDefinition_whenEventDefinitionHasTarget() throws Exception {
        // Arrange
        LinkEventDefinition eventDefinition = new LinkEventDefinition();
        eventDefinition.setTarget("target");
        eventDefinition.setName("name");
        eventDefinition.setId("id");

        // Act
        String generatedXml = convertToXml(eventDefinition);
        String expectedXml = """
            <bpmn2:linkEventDefinition id="id" name="name">
                <bpmn2:target>target</bpmn2:target>
             </bpmn2:linkEventDefinition>
            """;
        // Assert
        assertThat(generatedXml).isEqualToIgnoringWhitespace(expectedXml);
    }

    private static String convertToXml(LinkEventDefinition eventDefinition) throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter xtw = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
        LinkEventDefinitionXMLConverter linkEventDefinitionXMLConverter = new LinkEventDefinitionXMLConverter();

        linkEventDefinitionXMLConverter.writeLinkDefinition(eventDefinition, xtw);

        return stringWriter.toString();
    }

    @Test
    public void should_writeLinkDefinition_whenEventDefinitionHasSources() throws Exception {
        // Arrange
        LinkEventDefinition eventDefinition = new LinkEventDefinition();

        eventDefinition.setName("name");
        eventDefinition.setId("id");
        eventDefinition.addSource("source1");
        eventDefinition.addSource("source2");

        // Act
        String generatedXml = convertToXml(eventDefinition);
        String expectedXml = """
            <bpmn2:linkEventDefinition id="id" name="name">
                <bpmn2:source>source1</bpmn2:source>
                <bpmn2:source>source2</bpmn2:source>
            </bpmn2:linkEventDefinition>
            """;
        // Assert
        assertThat(generatedXml).isEqualToIgnoringWhitespace(expectedXml);
    }

    @Test
    public void should_writeLinkDefinition_whenEventDefinitionNameIsEmpty() throws Exception {
        // Arrange
        LinkEventDefinition eventDefinition = new LinkEventDefinition();

        eventDefinition.setName("");
        eventDefinition.setId("id");
        eventDefinition.addSource("source1");

        // Act
        String generatedXml = convertToXml(eventDefinition);
        String expectedXml = """
            <bpmn2:linkEventDefinition id="id" name="">
                <bpmn2:source>source1</bpmn2:source>
            </bpmn2:linkEventDefinition>
            """;
        // Assert
        assertThat(generatedXml).isEqualToIgnoringWhitespace(expectedXml);
    }
}
