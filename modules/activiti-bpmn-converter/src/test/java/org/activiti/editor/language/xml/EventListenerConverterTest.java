package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventListener;
import org.activiti.bpmn.model.Process;
import org.junit.Test;

/**
 * Test for ACT-1657
 * @author Frederik Heremans
 */
public class EventListenerConverterTest extends AbstractConverterTest {

  @Test
  public void connvertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }
  
  protected String getResource() {
    return "eventlistenersmodel.bpmn20.xml";
  }
  
  private void validateModel(BpmnModel model) {
    Process process = model.getMainProcess();
    assertNotNull(process);
    assertNotNull(process.getEventListeners());
    assertEquals(3, process.getEventListeners().size());
    
    // Listener with class
    EventListener listener = process.getEventListeners().get(0);
    assertEquals("ENTITY_CREATE", listener.getEvents());
    assertEquals("org.activiti.test.MyListener", listener.getImplementation());
    assertEquals(BpmnXMLConstants.ATTRIBUTE_LISTENER_CLASS, listener.getImplementationType());
    
    // Listener with class, but no specific event (== all events)
    listener = process.getEventListeners().get(1);
    assertNull(listener.getEvents());
    assertEquals("org.activiti.test.AllEventTypesListener", listener.getImplementation());
    assertEquals(BpmnXMLConstants.ATTRIBUTE_LISTENER_CLASS, listener.getImplementationType());
    
    // Listener with delegate expression
    listener = process.getEventListeners().get(2);
    assertEquals("ENTITY_DELETE", listener.getEvents());
    assertEquals("${myListener}", listener.getImplementation());
    assertEquals(BpmnXMLConstants.ATTRIBUTE_LISTENER_DELEGATEEXPRESSION, listener.getImplementationType());
  }
}
