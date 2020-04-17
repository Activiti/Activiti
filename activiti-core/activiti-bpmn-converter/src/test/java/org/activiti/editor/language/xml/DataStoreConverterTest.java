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
