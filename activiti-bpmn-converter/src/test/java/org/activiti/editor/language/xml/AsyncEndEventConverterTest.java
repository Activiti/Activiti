package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.*;
import org.junit.Test;

public class AsyncEndEventConverterTest extends AbstractConverterTest {

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
    deployProcess(parsedModel);
  }
  
  protected String getResource() {
    return "asyncendeventmodel.bpmn";
  }
  
  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("endEvent");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof EndEvent);
    assertEquals("endEvent", flowElement.getId());
    EndEvent endEvent = (EndEvent) flowElement;
    assertEquals("endEvent", endEvent.getId());
    assertTrue(endEvent.isAsynchronous());
    
    List<ActivitiListener> listeners = endEvent.getExecutionListeners();
    assertEquals(1, listeners.size());
    ActivitiListener listener = listeners.get(0);
    assertTrue(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType()));
    assertEquals("org.test.TestClass", listener.getImplementation());
    assertEquals("start", listener.getEvent());

    assertEquals(1, endEvent.getIncomingFlows().size());
    SequenceFlow sequence = endEvent.getIncomingFlows().get(0);
    assertEquals("sid-91C0F3A0-649F-462E-A1C1-1CE499FEDE3E", sequence.getId());
  }
}
