package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataStore;
import org.activiti.bpmn.model.MessageFlow;
import org.activiti.bpmn.model.Pool;
import org.junit.Test;

public class MessageFlowConverterTest extends AbstractConverterTest {

  @Test
  public void connvertXMLToModel() throws Exception {
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
    assertEquals(1, model.getDataStores().size());
    DataStore dataStore = model.getDataStore("DATASTORE_1");
    assertNotNull(dataStore);
    assertEquals("DATASTORE_1", dataStore.getId());
    assertEquals("test", dataStore.getName());
    assertEquals("ITEM_1", dataStore.getItemSubjectRef());
    
    MessageFlow messageFlow = model.getMessageFlow("MESSAGEFLOW_1");
    assertNotNull(messageFlow);
    assertEquals("test 1", messageFlow.getName());
    assertEquals("task1", messageFlow.getSourceRef());
    assertEquals("task2", messageFlow.getTargetRef());
    
    messageFlow = model.getMessageFlow("MESSAGEFLOW_2");
    assertNotNull(messageFlow);
    assertEquals("test 2", messageFlow.getName());
    assertEquals("task2", messageFlow.getSourceRef());
    assertEquals("task3", messageFlow.getTargetRef());
    
    assertEquals(2, model.getPools().size());
    Pool pool = model.getPools().get(0);
    assertEquals("participant1", pool.getId());
    assertEquals("Participant 1", pool.getName());
    assertEquals("PROCESS_1", pool.getProcessRef());
    
    pool = model.getPools().get(1);
    assertEquals("participant2", pool.getId());
    assertEquals("Participant 2", pool.getName());
    assertEquals("PROCESS_2", pool.getProcessRef());
  }
}
