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
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.Event;

/**
 * @author Tijs Rademakers
 */
public class ErrorEventDefinitionParser extends BaseChildElementParser {

  public String getElementName() {
    return ELEMENT_EVENT_ERRORDEFINITION;
  }
  
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
    if (parentElement instanceof Event == false) return;
    
    ErrorEventDefinition eventDefinition = new ErrorEventDefinition();
    BpmnXMLUtil.addXMLLocation(eventDefinition, xtr);
    eventDefinition.setErrorCode(xtr.getAttributeValue(null, "errorRef"));
    
    BpmnXMLUtil.parseChildElements(ELEMENT_EVENT_ERRORDEFINITION, eventDefinition, xtr, model);
    
    ((Event) parentElement).getEventDefinitions().add(eventDefinition);
  }
}
