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

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Error;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

public class ErrorConverterTest extends AbstractConverterTest {

    @Test
    public void testConversionFromXmlToBPMNModel() throws Exception {
        BpmnModel bpmnModel = readXMLFile();
        assertThat(bpmnModel.getErrors().values())
                .usingRecursiveFieldByFieldElementComparator()
                .isSubsetOf(newArrayList(new Error("Error_0v4rsz5",
                                                               "ok",
                                                               "200"),
                                                     new Error("Error_02htlc0",
                                                               "conflict",
                                                               "409")));
    }

    @Test
    public void testConversionFromBPMNModelToXml() throws Exception {
        BpmnModel bpmnModel = readXMLFile();
        byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel);
        String convertedXml = new String(xml, StandardCharsets.UTF_8);
        assertThat(convertedXml).contains("<error id=\"Error_0v4rsz5\" name=\"ok\" errorCode=\"200\">");
        assertThat(convertedXml).contains("<error id=\"Error_02htlc0\" name=\"conflict\" errorCode=\"409\">");
    }

    @Override
    protected String getResource() {
        return "error.bpmn";
    }
}
