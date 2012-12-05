package org.activiti.bpmn.converter.util;

import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.apache.commons.lang.StringUtils;

public class ActivitiListenerUtil implements BpmnXMLConstants {

  public static void writeListeners(BaseElement element, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
    List<ActivitiListener> listenerList = null;
    String xmlElementName = ELEMENT_EXECUTION_LISTENER;
    if (element instanceof UserTask) {
      listenerList = ((UserTask) element).getTaskListeners();
      xmlElementName = ELEMENT_TASK_LISTENER;
    } else if (element instanceof Activity) {
      listenerList = ((Activity) element).getExecutionListeners();
    } else if (element instanceof Process) {
      listenerList = ((Process) element).getExecutionListeners();
    } else if (element instanceof SequenceFlow) {
      listenerList = ((SequenceFlow) element).getExecutionListeners();
    }
    
    if (listenerList != null) {
    
      for (ActivitiListener listener : listenerList) {
        
        if (StringUtils.isNotEmpty(listener.getEvent())) {
          
          if (didWriteExtensionStartElement == false) { 
            xtw.writeStartElement(ELEMENT_EXTENSIONS);
            didWriteExtensionStartElement = true;
          }
          
          xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, xmlElementName, ACTIVITI_EXTENSIONS_NAMESPACE);
          BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_EVENT, listener.getEvent(), xtw);
          
          if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType())) {
            BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_CLASS, listener.getImplementation(), xtw);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(listener.getImplementationType())) {
            BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_EXPRESSION, listener.getImplementation(), xtw);
          } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(listener.getImplementationType())) {
            BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_DELEGATEEXPRESSION, listener.getImplementation(), xtw);
          }
          
          xtw.writeEndElement();
        }
      }
    }
  }
  
}
