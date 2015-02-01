package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataStore;
import org.activiti.bpmn.model.DataStoreReference;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Pool;
import org.junit.Test;

public class DataStoreConverterTest extends AbstractConverterTest {

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
    return "datastore.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    assertEquals(1, model.getDataStores().size());
    DataStore dataStore = model.getDataStore("DataStore_1");
    assertNotNull(dataStore);
    assertEquals("DataStore_1", dataStore.getId());
    assertEquals("test", dataStore.getDataState());
    assertEquals("Test Database", dataStore.getName());
    assertEquals("test", dataStore.getItemSubjectRef());
    
    FlowElement refElement = model.getFlowElement("DataStoreReference_1");
    assertNotNull(refElement);
    assertTrue(refElement instanceof DataStoreReference);
    
    assertEquals(1, model.getPools().size());
    Pool pool = model.getPools().get(0);
    assertEquals("pool1", pool.getId());
  }
}
