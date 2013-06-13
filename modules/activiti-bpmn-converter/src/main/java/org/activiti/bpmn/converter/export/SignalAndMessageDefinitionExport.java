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
      for (FlowElement flowElement : process.findFlowElementsOfType(Event.class)) {
        Event event = (Event) flowElement;
        if (event.getEventDefinitions().size() > 0) {
          EventDefinition eventDefinition = event.getEventDefinitions().get(0);
          if (eventDefinition instanceof SignalEventDefinition) {
            SignalEventDefinition signalEvent = (SignalEventDefinition) eventDefinition;
            if (model.containsSignalId(signalEvent.getSignalRef()) == false) {
              Signal signal = new Signal(signalEvent.getSignalRef(), signalEvent.getSignalRef());
              model.addSignal(signal);
            }

          } else if (eventDefinition instanceof MessageEventDefinition) {
            MessageEventDefinition messageEvent = (MessageEventDefinition) eventDefinition;
            if (model.containsMessageId(messageEvent.getMessageRef()) == false) {
              Message message = new Message(messageEvent.getMessageRef(), messageEvent.getMessageRef(), null);
              model.addMessage(message);
            }
          }
        }
      }
    }
    
    for (Signal signal : model.getSignals()) {
      xtw.writeStartElement(ELEMENT_SIGNAL);
      xtw.writeAttribute(ATTRIBUTE_ID, signal.getId());
      xtw.writeAttribute(ATTRIBUTE_NAME, signal.getName());
      if (signal.getScope() != null) {
        xtw.writeAttribute(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_SCOPE, signal.getScope());
      }
      xtw.writeEndElement();
    }
    
    for (Message message : model.getMessages()) {
      xtw.writeStartElement(ELEMENT_MESSAGE);
      String messageId = message.getId();
      // remove the namespace from the message id if set
      if (model.getTargetNamespace() != null && messageId.startsWith(model.getTargetNamespace())) {
        messageId = messageId.replace(model.getTargetNamespace(), "");
        messageId = messageId.replaceFirst(":", "");
      } else {
        for (String prefix : model.getNamespaces().keySet()) {
          String namespace = model.getNamespace(prefix);
          if (messageId.startsWith(namespace)) {
            messageId = messageId.replace(model.getTargetNamespace(), "");
            messageId = prefix + messageId;
          }
        }
      }
      xtw.writeAttribute(ATTRIBUTE_ID, messageId);
      xtw.writeAttribute(ATTRIBUTE_NAME, message.getName());
      xtw.writeEndElement();
    }
  }
}
