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

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataStore;
import org.activiti.bpmn.model.DataStoreReference;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Pool;
import org.junit.jupiter.api.Test;

public class DataStoreConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
  }

  protected String getResource() {
    return "datastore.bpmn";
  }

  private void validateModel(BpmnModel model) {
    assertThat(model.getDataStores()).hasSize(1);
    DataStore dataStore = model.getDataStore("DataStore_1");
    assertThat(dataStore).isNotNull();
    assertThat(dataStore.getId()).isEqualTo("DataStore_1");
    assertThat(dataStore.getDataState()).isEqualTo("test");
    assertThat(dataStore.getName()).isEqualTo("Test Database");
    assertThat(dataStore.getItemSubjectRef()).isEqualTo("test");

    FlowElement refElement = model.getFlowElement("DataStoreReference_1");
    assertThat(refElement).isNotNull();
    assertThat(refElement).isInstanceOf(DataStoreReference.class);

    assertThat(model.getPools()).hasSize(1);
    Pool pool = model.getPools().get(0);
    assertThat(pool.getId()).isEqualTo("pool1");
  }
}
