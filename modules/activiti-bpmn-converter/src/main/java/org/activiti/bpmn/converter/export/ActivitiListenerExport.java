/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.bpmn.converter.export;

import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.EventListener;
import org.activiti.bpmn.model.HasExecutionListeners;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.UserTask;
import org.apache.commons.lang3.StringUtils;

public class ActivitiListenerExport implements BpmnXMLConstants {

  public static boolean writeListeners(BaseElement element, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
    if (element instanceof HasExecutionListeners) {
      didWriteExtensionStartElement = writeListeners(ELEMENT_EXECUTION_LISTENER, ((HasExecutionListeners) element).getExecutionListeners(), didWriteExtensionStartElement, xtw);
    }
    // In case of a usertaks, also add task-listeners
    if (element instanceof UserTask) {
      didWriteExtensionStartElement = writeListeners(ELEMENT_TASK_LISTENER, ((UserTask) element).getTaskListeners(), didWriteExtensionStartElement, xtw);
    }
    
    // In case of a process-element, write the event-listeners
    if (element instanceof Process) {
    	didWriteExtensionStartElement = writeEventListeners(((Process) element).getEventListeners(), didWriteExtensionStartElement, xtw);
    }
    
    return didWriteExtensionStartElement;
  }
  
  protected static boolean writeEventListeners(List<EventListener> eventListeners,
      boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
  	
  	if(eventListeners != null && !eventListeners.isEmpty()) {
  		for(EventListener eventListener : eventListeners) {
  			if (!didWriteExtensionStartElement) { 
          xtw.writeStartElement(ELEMENT_EXTENSIONS);
          didWriteExtensionStartElement = true;
        }
  			
  			 xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, ELEMENT_EVENT_LISTENER, ACTIVITI_EXTENSIONS_NAMESPACE);
  			 BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_EVENTS, eventListener.getEvents(), xtw);
  			 BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_ENTITY_TYPE, eventListener.getEntityType(), xtw);
  			 
  			 if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(eventListener.getImplementationType())) {
           BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_CLASS, eventListener.getImplementation(), xtw);
           
         } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(eventListener.getImplementationType())) {
           BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_DELEGATEEXPRESSION, eventListener.getImplementation(), xtw);
           
         } else if (ImplementationType.IMPLEMENTATION_TYPE_THROW_SIGNAL_EVENT.equals(eventListener.getImplementationType())) {
        	 BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_THROW_SIGNAL_EVENT_NAME, eventListener.getImplementation(), xtw);
        	 BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_THROW_EVENT_TYPE, ATTRIBUTE_LISTENER_THROW_EVENT_TYPE_SIGNAL, xtw);
        	 
         } else if (ImplementationType.IMPLEMENTATION_TYPE_THROW_GLOBAL_SIGNAL_EVENT.equals(eventListener.getImplementationType())) {
        	 BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_THROW_SIGNAL_EVENT_NAME, eventListener.getImplementation(), xtw);
        	 BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_THROW_EVENT_TYPE, ATTRIBUTE_LISTENER_THROW_EVENT_TYPE_GLOBAL_SIGNAL, xtw);
        	 
         } else if (ImplementationType.IMPLEMENTATION_TYPE_THROW_MESSAGE_EVENT.equals(eventListener.getImplementationType())) {
        	 BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_THROW_MESSAGE_EVENT_NAME, eventListener.getImplementation(), xtw);
        	 BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_THROW_EVENT_TYPE, ATTRIBUTE_LISTENER_THROW_EVENT_TYPE_MESSAGE, xtw);
        	 
         } else if (ImplementationType.IMPLEMENTATION_TYPE_THROW_ERROR_EVENT.equals(eventListener.getImplementationType())) {
        	 BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_THROW_ERROR_EVENT_CODE, eventListener.getImplementation(), xtw);
        	 BpmnXMLUtil.writeDefaultAttribute(ATTRIBUTE_LISTENER_THROW_EVENT_TYPE, ATTRIBUTE_LISTENER_THROW_EVENT_TYPE_ERROR, xtw);
         }
  			 
  			 xtw.writeEndElement();
  		}
  	}
  	
	  return didWriteExtensionStartElement;
  }

	private static boolean writeListeners(String xmlElementName, List<ActivitiListener> listenerList, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
    if (listenerList != null) {
      
      for (ActivitiListener listener : listenerList) {
        
        if (StringUtils.isNotEmpty(listener.getEvent())) {
          
          if (!didWriteExtensionStartElement) { 
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
          
          FieldExtensionExport.writeFieldExtensions(listener.getFieldExtensions(), true, xtw);
          
          xtw.writeEndElement();
        }
      }
    }
    return didWriteExtensionStartElement;
  }
  
}
