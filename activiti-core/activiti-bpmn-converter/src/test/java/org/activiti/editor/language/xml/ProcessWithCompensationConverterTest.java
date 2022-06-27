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

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;


public class ProcessWithCompensationConverterTest {

  @Test
  public void testConvertingAfterAutoLayout() {

    final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("ProcessWithCompensationAssociation.bpmn20.xml");

    BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

    BpmnModel bpmnModel1 = bpmnXMLConverter.convertToBpmnModel(new InputStreamProvider() {

      @Override
      public InputStream getInputStream() {
        return inputStream;
      }
    }, false, false);

    if (bpmnModel1.getLocationMap().size() == 0) {
      BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnModel1);
      bpmnLayout.execute();
    }

    byte[] xmlByte = bpmnXMLConverter.convertToXML(bpmnModel1);
    final InputStream byteArrayInputStream = new ByteArrayInputStream(xmlByte);

    BpmnModel bpmnModel2 = bpmnXMLConverter.convertToBpmnModel(new InputStreamProvider() {

      @Override
      public InputStream getInputStream() {
        return byteArrayInputStream;
      }
    }, false, false);

    assertThat(bpmnModel1.getLocationMap()).hasSize(10);
    assertThat(bpmnModel2.getLocationMap()).hasSize(10);

    assertThat(bpmnModel1.getFlowLocationMap()).hasSize(7);
    assertThat(bpmnModel2.getFlowLocationMap()).hasSize(7);
  }

}
