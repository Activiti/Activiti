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
package org.activiti.bpmn.converter.child;

import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.LinkEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkEventDefinitionParserTest {
    private LinkEventDefinitionParser parser = new LinkEventDefinitionParser();
    private XMLInputFactory xif = XMLInputFactory.newInstance();

    @Test
    public void parseChildElement_should_setLinkEventDefinitionProperties_forThrowEvent() throws Exception {
        try (InputStream xmlStream = this.getClass().getClassLoader()
            .getResourceAsStream("link-event-definition-with-target.xml")) {
            XMLStreamReader xtr = xif.createXMLStreamReader(xmlStream, "UTF-8");
            xtr.next();

            ThrowEvent intermediateThrowEvent = new ThrowEvent();
            parser.parseChildElement(xtr, intermediateThrowEvent, null);

            List<EventDefinition> eventDefinitionList = intermediateThrowEvent.getEventDefinitions();
            assertThat(eventDefinitionList).isNotEmpty();
            LinkEventDefinition linkEventDefinition = (LinkEventDefinition) eventDefinitionList.get(0);
            assertThat(linkEventDefinition).isNotNull();
            assertThat(linkEventDefinition.getId()).isEqualTo("LinkEventDefinition_03bs3ae");
            assertThat(linkEventDefinition.getName()).isEqualTo("a");
            assertThat(linkEventDefinition.getTarget()).isEqualTo("LinkEventDefinition_1smqlx0");
        }
    }

    @Test
    public void parseChildElement_should_setLinkEventDefinitionProperties_forCatchEvent() throws Exception {
        try (InputStream xmlStream = this.getClass().getClassLoader()
            .getResourceAsStream("link-event-definition-with-source.xml")) {
            XMLStreamReader xtr = xif.createXMLStreamReader(xmlStream, "UTF-8");
            xtr.next();

            IntermediateCatchEvent intermediateCatchEvent = new IntermediateCatchEvent();
            parser.parseChildElement(xtr, intermediateCatchEvent, null);

            List<EventDefinition> eventDefinitionList = intermediateCatchEvent.getEventDefinitions();
            assertThat(eventDefinitionList).isNotEmpty();
            LinkEventDefinition linkEventDefinition = (LinkEventDefinition) eventDefinitionList.get(0);
            assertThat(linkEventDefinition).isNotNull();
            assertThat(linkEventDefinition.getId()).isEqualTo("LinkEventDefinition_1smqlx0");
            assertThat(linkEventDefinition.getName()).isEqualTo("a");
            assertThat(linkEventDefinition.getSources().get(0)).isEqualTo("LinkEventDefinition_03bs3ae");
            assertThat(linkEventDefinition.getSources().get(1)).isEqualTo("test");
        }
    }
}
