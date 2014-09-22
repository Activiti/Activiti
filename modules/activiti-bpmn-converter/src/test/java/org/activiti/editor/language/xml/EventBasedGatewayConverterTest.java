package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.junit.Test;

/**
 * Test for ACT-1657
 * @author Frederik Heremans
 */
public class EventBasedGatewayConverterTest extends AbstractConverterTest {

  @Test
  public void connvertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }
  
  protected String getResource() {
    return "eventgatewaymodel.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("eventBasedGateway");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof EventGateway);
    
    EventGateway gateway = (EventGateway) flowElement;
    List<ActivitiListener> listeners = gateway.getExecutionListeners();
    assertEquals(1, listeners.size());
    ActivitiListener listener = listeners.get(0);
    assertTrue(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType()));
    assertEquals("org.test.TestClass", listener.getImplementation());
    assertEquals("start", listener.getEvent());
  }
}
