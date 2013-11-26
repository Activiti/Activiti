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
package org.activiti.bpmn.converter.child;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.EventListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Frederik Heremans
 */
public class ActivitiEventListenerParser extends BaseChildElementParser {
  
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
    
    EventListener listener = new EventListener();
    BpmnXMLUtil.addXMLLocation(listener, xtr);
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_LISTENER_CLASS))) {
      listener.setImplementation(xtr.getAttributeValue(null, ATTRIBUTE_LISTENER_CLASS));
      listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
    } else if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_LISTENER_DELEGATEEXPRESSION))) {
      listener.setImplementation(xtr.getAttributeValue(null, ATTRIBUTE_LISTENER_DELEGATEEXPRESSION));
      listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
    } else {
      model.addProblem("Element 'class' or 'delegateExpression' is mandatory on eventListener", xtr);
    }
    listener.setEvents(xtr.getAttributeValue(null, ATTRIBUTE_LISTENER_EVENTS));
    
    Process parentProcess = (Process) parentElement;
    parentProcess.getEventListeners().add(listener);
  }
  
  @Override
  public String getElementName() {
    return ELEMENT_EVENT_LISTENER;
  }
}
