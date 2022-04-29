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

import java.io.IOException;
import java.io.InputStream;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PrefixConversionTest {

    @Test
    public void shouldConvertWithoutDoublingTheBPMN2PrefixWhenAbpmnFileWithPrefixIsGiven() throws IOException {
        final InputStream originalXMLStream = this.getClass().getClassLoader().getResourceAsStream("checkConversionPrefix.bpmn");
        final InputStream copiedXMLStream = this.getClass().getClassLoader().getResourceAsStream("checkConversionPrefix.bpmn");
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

        byte[] byteOriginalXMLStream = new byte[originalXMLStream.available()];
        originalXMLStream.read(byteOriginalXMLStream);


        BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(new InputStreamProvider() {

            @Override
            public InputStream getInputStream() {
                return copiedXMLStream;
            }
        }, false, false);

        byte[] bytesReconvertedXMLModel = bpmnXMLConverter.convertToXML(bpmnModel);
        String stringifyOriginalXML = new String(byteOriginalXMLStream);
        String stringifyReconvertedModel = new String(bytesReconvertedXMLModel);

        assertThat(stringifyReconvertedModel).isXmlEqualTo(stringifyOriginalXML);
    }
}
