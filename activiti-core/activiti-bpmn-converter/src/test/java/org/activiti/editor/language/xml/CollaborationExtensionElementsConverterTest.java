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

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Pool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CollaborationExtensionElementsConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    Assertions.assertNotNull(bpmnModel);
    assertThat(bpmnModel.getPools()).isNotEmpty();
    Pool pool1 = bpmnModel.getPool("BP01");
    Assertions.assertNotNull(pool1);
    Assertions.assertEquals("Pool", pool1.getName());
    Pool pool2 = bpmnModel.getPool("BP02");
    Assertions.assertNotNull(pool2);
    Assertions.assertEquals("Pool2", pool2.getName());
  }

  protected String getResource() {
    return "collaborationExtensionElements.bpmn";
  }
}
