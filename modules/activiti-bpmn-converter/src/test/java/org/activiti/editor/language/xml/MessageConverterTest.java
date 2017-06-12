package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.junit.Test;

public class MessageConverterTest extends AbstractConverterTest {
  
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
  
  private void validateModel(BpmnModel model) {
    Message message = model.getMessage("writeReport");
    assertNotNull(message);
    assertEquals("Examples:writeReportItem", message.getItemRef());
    assertEquals("newWriteReport", message.getName());
    assertEquals("writeReport", message.getId());
    
    FlowElement flowElement = model.getFlowElement("theStart");
    if(flowElement instanceof StartEvent){
      StartEvent startEvent = (StartEvent) flowElement;
      if(startEvent.getEventDefinitions().get(0) instanceof MessageEventDefinition){
        MessageEventDefinition messageEventDefinition = (MessageEventDefinition) startEvent.getEventDefinitions().get(0);
        assertNotNull("Attribute messageRef of messageEventDefinition can't be null.", messageEventDefinition.getMessageRef());
        //if messageEventDefinition is not null test messageRef stores messageId 
        assertTrue("MessageRef attribute of messageEventDefinition of start event should be equal to messageId.", (messageEventDefinition.getMessageRef()).equals(message.getId()));
      }
    }

    Message message2 = model.getMessage("writeReport2");
    assertNotNull(message2);
    assertEquals("http://foo.bar.com/Examples:writeReportItem2", message2.getItemRef());
    assertEquals("newWriteReport2", message2.getName());
    assertEquals("writeReport2", message2.getId());
  }
  
  protected String getResource() {
        return "message.bpmn";
  }
}