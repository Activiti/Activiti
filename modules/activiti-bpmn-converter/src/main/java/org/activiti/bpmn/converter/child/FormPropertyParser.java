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

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;

/**
 * @author Tijs Rademakers
 */
public class FormPropertyParser extends BaseChildElementParser {

  public String getElementName() {
    return "formProperty";
  }
  
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement) throws Exception {
    if (parentElement instanceof UserTask == false && parentElement instanceof StartEvent == false) return;
    
    FormProperty property = new FormProperty();
    property.setId(xtr.getAttributeValue(null, "id"));
    property.setName(xtr.getAttributeValue(null, "name"));
    property.setType(xtr.getAttributeValue(null, "type"));
    property.setValue(xtr.getAttributeValue(null, "value"));
    property.setVariable(xtr.getAttributeValue(null, "variable"));
    property.setExpression(xtr.getAttributeValue(null, "expression"));
    property.setDefaultExpression(xtr.getAttributeValue(null, "default"));
    property.setDatePattern(xtr.getAttributeValue(null, "datePattern"));
    property.setRequired(Boolean.valueOf(xtr.getAttributeValue(null, "required")));
    property.setReadable(Boolean.valueOf(xtr.getAttributeValue(null, "readable")));
    property.setWriteable(Boolean.valueOf(xtr.getAttributeValue(null, "writable")));
    
    boolean readyWithFormProperty = false;
    try {
      while (readyWithFormProperty == false && xtr.hasNext()) {
        xtr.next();
        if (xtr.isStartElement() && "value".equalsIgnoreCase(xtr.getLocalName())) {
          FormValue value = new FormValue();
          value.setId(xtr.getAttributeValue(null, "id"));
          value.setName(xtr.getAttributeValue(null, "name"));
          property.getFormValues().add(value);

        } else if (xtr.isEndElement() && getElementName().equalsIgnoreCase(xtr.getLocalName())) {
          readyWithFormProperty = true;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Error parsing form properties child elements", e);
    }
    
    if (parentElement instanceof UserTask) {
      ((UserTask) parentElement).getFormProperties().add(property);
    } else {
      ((StartEvent) parentElement).getFormProperties().add(property);
    }
  }
}
