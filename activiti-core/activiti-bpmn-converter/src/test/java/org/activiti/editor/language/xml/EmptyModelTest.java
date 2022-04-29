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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.activiti.bpmn.exceptions.XMLException;
import org.junit.jupiter.api.Test;

public class EmptyModelTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    assertThatExceptionOfType(XMLException.class)
      .isThrownBy(() -> readXMLFile());
  }

  @Test
  public void convertModelToXML() throws Exception {
    assertThatExceptionOfType(XMLException.class)
      .isThrownBy(() -> readXMLFile());
  }

  protected String getResource() {
    return "empty.bpmn";
  }
}
