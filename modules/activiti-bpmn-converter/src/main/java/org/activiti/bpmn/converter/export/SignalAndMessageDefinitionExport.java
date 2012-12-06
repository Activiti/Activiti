package org.activiti.bpmn.converter.export;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;

public class SignalAndMessageDefinitionExport implements BpmnXMLConstants {

  public static void writeSignalsAndMessages(BpmnModel model, XMLStreamWriter xtw) throws Exception {
    
    for (Process process : model.getProcesses()) {
      for (FlowElement flowElement : process.getFlowElements()) {
        if (flowElement instanceof Event) {
          Event event = (Event) flowElement;
          if (event.getEventDefinitions().size() > 0) {
            EventDefinition eventDefinition = event.getEventDefinitions().get(0);
            if (eventDefinition instanceof SignalEventDefinition) {
              SignalEventDefinition signalEvent = (SignalEventDefinition) eventDefinition;
              if (model.containsSignalId(signalEvent.getSignalRef()) == false) {
                model.addSignal(signalEvent.getSignalRef(), signalEvent.getSignalRef());
              }
              
            } else if (eventDefinition instanceof MessageEventDefinition) {
              MessageEventDefinition messageEvent = (MessageEventDefinition) eventDefinition;
              if (model.containsMessageId(messageEvent.getMessageRef()) == false) {
                model.addMessage(messageEvent.getMessageRef(), messageEvent.getMessageRef());
              }
            }
          }
        }
      }
    }
    
    for (Signal signal : model.getSignals()) {
      xtw.writeStartElement(ELEMENT_SIGNAL);
      xtw.writeAttribute(ATTRIBUTE_ID, signal.getId());
      xtw.writeAttribute(ATTRIBUTE_NAME, signal.getName());
      xtw.writeEndElement();
    }
    
    for (Message message : model.getMessages()) {
      xtw.writeStartElement(ELEMENT_MESSAGE);
      xtw.writeAttribute(ATTRIBUTE_ID, message.getId());
      xtw.writeAttribute(ATTRIBUTE_NAME, message.getName());
      xtw.writeEndElement();
    }
  }
}
