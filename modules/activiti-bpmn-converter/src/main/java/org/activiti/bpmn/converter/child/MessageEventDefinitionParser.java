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
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class MessageEventDefinitionParser extends BaseChildElementParser {

  public String getElementName() {
    return ELEMENT_EVENT_MESSAGEDEFINITION;
  }
  
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
    if (parentElement instanceof Event == false) return;
    
    MessageEventDefinition eventDefinition = new MessageEventDefinition();
    BpmnXMLUtil.addXMLLocation(eventDefinition, xtr);
    eventDefinition.setMessageRef(xtr.getAttributeValue(null, ATTRIBUTE_MESSAGE_REF));
    
    if(!StringUtils.isEmpty(eventDefinition.getMessageRef())) {
      
      int indexOfP = eventDefinition.getMessageRef().indexOf(':');
      if (indexOfP != -1) {
        String prefix = eventDefinition.getMessageRef().substring(0, indexOfP);
        String resolvedNamespace = model.getNamespace(prefix);
        eventDefinition.setMessageRef(resolvedNamespace + ":" + eventDefinition.getMessageRef().substring(indexOfP + 1));
      } else {
        eventDefinition.setMessageRef(model.getTargetNamespace() + ":" + eventDefinition.getMessageRef());
      }
     
    }
    
    BpmnXMLUtil.parseChildElements(ELEMENT_EVENT_MESSAGEDEFINITION, eventDefinition, xtr, model);
    
    ((Event) parentElement).getEventDefinitions().add(eventDefinition);
  }
}
