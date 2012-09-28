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
package org.activiti.editor.language.bpmn.parser;

import java.util.logging.Level;

import javax.xml.stream.XMLStreamReader;

import org.activiti.editor.language.bpmn.model.ActivitiListener;
import org.activiti.editor.language.bpmn.model.BaseElement;
import org.activiti.editor.language.bpmn.model.FieldExtension;
import org.activiti.editor.language.bpmn.model.ServiceTask;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class FieldExtensionParser extends BaseChildElementParser {

  public String getElementName() {
    return "field";
  }
  
  public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement) throws Exception {
    if (parentElement instanceof ActivitiListener == false && parentElement instanceof ServiceTask == false) return;
    
    FieldExtension extension = new FieldExtension();
    extension.setFieldName(xtr.getAttributeValue(null, "name"));
    
    if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, "stringValue"))) {
      extension.setExpression(xtr.getAttributeValue(null, "stringValue"));

    } else if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, "expression"))) {
      extension.setExpression(xtr.getAttributeValue(null, "expression"));

    } else {
      boolean readyWithFieldExtension = false;
      try {
        while (readyWithFieldExtension == false && xtr.hasNext()) {
          xtr.next();
          if (xtr.isStartElement() && "string".equalsIgnoreCase(xtr.getLocalName())) {
            extension.setExpression(xtr.getElementText().trim());

          } else if (xtr.isStartElement() && "expression".equalsIgnoreCase(xtr.getLocalName())) {
            extension.setExpression(xtr.getElementText().trim());

          } else if (xtr.isEndElement() && getElementName().equalsIgnoreCase(xtr.getLocalName())) {
            readyWithFieldExtension = true;
          }
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Error parsing field extension child elements", e);
      }
    }
    
    if (parentElement instanceof ActivitiListener) {
      ((ActivitiListener) parentElement).getFieldExtensions().add(extension);
    } else {
      ((ServiceTask) parentElement).getFieldExtensions().add(extension);
    }
  }
}
