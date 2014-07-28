package org.activiti.editor.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.junit.Test;


public class CatchEventConverterTest extends AbstractConverterTest {

  @Test
  public void connvertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }
  
  @Test 
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }
  
  protected String getResource() {
    return "test.catcheventmodel.json";
  }
  
  private void validateModel(BpmnModel model) {
    
    FlowElement timerElement = model.getMainProcess().getFlowElement("timer_evt");
    EventDefinition timerEvent = extractEventDefinition(timerElement);
    assertTrue(timerEvent instanceof TimerEventDefinition);
    TimerEventDefinition ted = (TimerEventDefinition)timerEvent;
    assertEquals("PT5M", ted.getTimeDuration());
    
    FlowElement signalElement = model.getMainProcess().getFlowElement("signal_evt");
    EventDefinition signalEvent = extractEventDefinition(signalElement);
    assertTrue(signalEvent instanceof SignalEventDefinition);
    SignalEventDefinition sed = (SignalEventDefinition)signalEvent;
    assertEquals("signal_ref", sed.getSignalRef());
    
    FlowElement messageElement = model.getMainProcess().getFlowElement("message_evt");
    EventDefinition messageEvent = extractEventDefinition(messageElement);
    assertTrue(messageEvent instanceof MessageEventDefinition);
    MessageEventDefinition med = (MessageEventDefinition)messageEvent;
    assertEquals("message_ref", med.getMessageRef());
    
  }
  
}
