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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.Test;

public class BpmnXMLConverterTest {
    
    private BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
    private SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    @Test
    public void should_createSchema_when_pathContainsDecodedUTF8Characters() throws Exception {
        assertThatCode(() -> {
            bpmnXMLConverter.createSchema(factory, getDecodedUrl("你好/Main.xsd"));
        }).doesNotThrowAnyException();
    }

    private URL getDecodedUrl(String path) throws MalformedURLException {
        URL resource = getClass().getClassLoader().getResource(path);
        String decodedURL = URLDecoder.decode(resource.toExternalForm(), StandardCharsets.UTF_8);
        return new URL(decodedURL);
    }

    @Test
    public void should_createSchema() throws Exception { 
        Schema schema = bpmnXMLConverter.createSchema(factory, getClass().getClassLoader()
                .getResource("org/activiti/impl/bpmn/parser/BPMN20.xsd"));
        assertThat(schema).isNotNull();
    }
    
}
