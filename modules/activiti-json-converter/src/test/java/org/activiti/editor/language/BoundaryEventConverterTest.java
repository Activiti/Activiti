package org.activiti.editor.language;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.junit.Test;


public class BoundaryEventConverterTest extends AbstractConverterTest {

  @Test
  public void connvertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }
  
  @Test 
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    bpmnModel = convertToJsonAndBack(bpmnModel);
    System.out.println("xml " + new String(new BpmnXMLConverter().convertToXML(bpmnModel), "utf-8"));
    validateModel(bpmnModel);
  }
  
  @Override
  protected String getResource() {
    return "test.boundaryeventmodel.json";
  }

  private void validateModel(BpmnModel model) {
    
    FlowElement errorElement = model.getMainProcess().getFlowElement("errorEvent");
    assertTrue(errorElement instanceof BoundaryEvent);
    
    FlowElement signalElement = model.getMainProcess().getFlowElement("signalEvent");
    assertTrue(signalElement instanceof BoundaryEvent);
    
    FlowElement timerElement = model.getMainProcess().getFlowElement("timerEvent");
    assertTrue(timerElement instanceof BoundaryEvent);
    
    BoundaryEvent errorEvent  = (BoundaryEvent) errorElement;
    assertTrue(errorEvent.isCancelActivity()); //always true
    
    BoundaryEvent signalEvent  = (BoundaryEvent) signalElement;
    assertFalse(signalEvent.isCancelActivity());
    
    BoundaryEvent timerEvent  = (BoundaryEvent) timerElement;
    assertFalse(timerEvent.isCancelActivity());

  }
  
}
