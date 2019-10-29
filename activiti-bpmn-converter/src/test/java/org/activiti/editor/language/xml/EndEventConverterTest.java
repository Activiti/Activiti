package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.junit.Test;

public class EndEventConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(exportAndReadXMLFile(bpmnModel));
  }

  @Override
  protected String getResource() {
    return "end-error-event.bpmn20.xml";
  }

  private void validateModel(BpmnModel model) {
    assertEquals(2, model.getDefinitionsAttributes().size());
    
    FlowElement flowElement = model.getMainProcess().getFlowElement("EndEvent_0mdpjzn");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof EndEvent);
    
    EndEvent endEvent = (EndEvent) flowElement;
    
    assertTrue(endEvent.getIncomingFlows().size() == 1);
    assertTrue(endEvent.getEventDefinitions().size() == 1);
    
    //Check that incoming xml element is coming before error event definition
    assertTrue(endEvent.getIncomingFlows().get(0).getXmlRowNumber() < endEvent.getEventDefinitions().get(0).getXmlRowNumber());
    
    assertTrue(endEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition);
    ErrorEventDefinition errorEventDefinition = (ErrorEventDefinition) endEvent.getEventDefinitions().get(0);
    
    assertTrue(errorEventDefinition.getErrorRef().equals("Error_01agmko"));
  }
}
