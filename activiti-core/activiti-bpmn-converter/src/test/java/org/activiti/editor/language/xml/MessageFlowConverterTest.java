package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataStore;
import org.activiti.bpmn.model.MessageFlow;
import org.activiti.bpmn.model.Pool;
import org.junit.jupiter.api.Test;

public class MessageFlowConverterTest extends AbstractConverterTest {

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
    return "messageflow.bpmn";
  }

  private void validateModel(BpmnModel model) {
    assertThat(model.getDataStores()).hasSize(1);
    DataStore dataStore = model.getDataStore("DATASTORE_1");
    assertThat(dataStore).isNotNull();
    assertThat(dataStore.getId()).isEqualTo("DATASTORE_1");
    assertThat(dataStore.getName()).isEqualTo("test");
    assertThat(dataStore.getItemSubjectRef()).isEqualTo("ITEM_1");

    MessageFlow messageFlow = model.getMessageFlow("MESSAGEFLOW_1");
    assertThat(messageFlow).isNotNull();
    assertThat(messageFlow.getName()).isEqualTo("test 1");
    assertThat(messageFlow.getSourceRef()).isEqualTo("task1");
    assertThat(messageFlow.getTargetRef()).isEqualTo("task2");

    messageFlow = model.getMessageFlow("MESSAGEFLOW_2");
    assertThat(messageFlow).isNotNull();
    assertThat(messageFlow.getName()).isEqualTo("test 2");
    assertThat(messageFlow.getSourceRef()).isEqualTo("task2");
    assertThat(messageFlow.getTargetRef()).isEqualTo("task3");

    assertThat(model.getPools()).hasSize(2);
    Pool pool = model.getPools().get(0);
    assertThat(pool.getId()).isEqualTo("participant1");
    assertThat(pool.getName()).isEqualTo("Participant 1");
    assertThat(pool.getProcessRef()).isEqualTo("PROCESS_1");

    pool = model.getPools().get(1);
    assertThat(pool.getId()).isEqualTo("participant2");
    assertThat(pool.getName()).isEqualTo("Participant 2");
    assertThat(pool.getProcessRef()).isEqualTo("PROCESS_2");
  }
}
