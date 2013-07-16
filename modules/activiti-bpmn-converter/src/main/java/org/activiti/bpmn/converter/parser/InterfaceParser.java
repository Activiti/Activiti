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
package org.activiti.bpmn.converter.parser;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Interface;
import org.activiti.bpmn.model.Operation;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tijs Rademakers
 */
public class InterfaceParser implements BpmnXMLConstants {
  
  protected static final Logger LOGGER = Logger.getLogger(InterfaceParser.class.getName());
  
  public void parse(XMLStreamReader xtr, BpmnModel model) throws Exception {
    
    Interface interfaceObject = new Interface();
    BpmnXMLUtil.addXMLLocation(interfaceObject, xtr);
    interfaceObject.setId(model.getTargetNamespace() + ":" + xtr.getAttributeValue(null, ATTRIBUTE_ID));
    interfaceObject.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
    interfaceObject.setImplementationRef(parseMessageRef(xtr.getAttributeValue(null, ATTRIBUTE_IMPLEMENTATION_REF), model));
    
    boolean readyWithInterface = false;
    Operation operation = null;
    try {
      while (readyWithInterface == false && xtr.hasNext()) {
        xtr.next();
        if (xtr.isStartElement() && ELEMENT_OPERATION.equals(xtr.getLocalName())) {
          operation = new Operation();
          BpmnXMLUtil.addXMLLocation(operation, xtr);
          operation.setId(model.getTargetNamespace() + ":" + xtr.getAttributeValue(null, ATTRIBUTE_ID));
          operation.setName(xtr.getAttributeValue(null, ATTRIBUTE_NAME));
          operation.setImplementationRef(parseMessageRef(xtr.getAttributeValue(null, ATTRIBUTE_IMPLEMENTATION_REF), model));

        } else if (xtr.isStartElement() && ELEMENT_IN_MESSAGE.equals(xtr.getLocalName())) {
          String inMessageRef = xtr.getElementText();
          if (operation != null && StringUtils.isNotEmpty(inMessageRef)) {
            operation.setInMessageRef(parseMessageRef(inMessageRef.trim(), model));
          }
          
        } else if (xtr.isStartElement() && ELEMENT_OUT_MESSAGE.equals(xtr.getLocalName())) {
          String outMessageRef = xtr.getElementText();
          if (operation != null && StringUtils.isNotEmpty(outMessageRef)) {
            operation.setOutMessageRef(parseMessageRef(outMessageRef.trim(), model));
          }
          
        } else if (xtr.isEndElement() && ELEMENT_OPERATION.equalsIgnoreCase(xtr.getLocalName())) {
          if (operation != null && StringUtils.isNotEmpty(operation.getImplementationRef())) {
            interfaceObject.getOperations().add(operation);
          }
          
        } else if (xtr.isEndElement() && ELEMENT_INTERFACE.equals(xtr.getLocalName())) {
          readyWithInterface = true;
        }
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Error parsing interface child elements", e);
    }
    
    model.getInterfaces().add(interfaceObject);
  }
  
  protected String parseMessageRef(String messageRef, BpmnModel model) {
    String result = null;
    if (StringUtils.isNotEmpty(messageRef)) {
      int indexOfP = messageRef.indexOf(':');
      if (indexOfP != -1) {
        String prefix = messageRef.substring(0, indexOfP);
        String resolvedNamespace = model.getNamespace(prefix);
        result = resolvedNamespace + ":" + messageRef.substring(indexOfP + 1);
      } else {
        result = model.getTargetNamespace() + ":" + messageRef;
      }
    }
    return result;
  }
}
