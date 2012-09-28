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
package org.activiti.editor.language.bpmn.export;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.editor.exception.XMLException;
import org.activiti.editor.json.constants.EditorJsonConstants;
import org.activiti.editor.stencilset.StencilConstants;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public abstract class BaseShapeToBpmnExport implements EditorJsonConstants, StencilConstants, ActivitiNamespaceConstants {
  
  protected static final Logger LOGGER = Logger.getLogger(BaseShapeToBpmnExport.class.getName());
  protected boolean didWriteExtensionStartElement;

  public void convert(ObjectNode objectNode, IndentingXMLStreamWriter xtw, ObjectNode modelNode) {
  	didWriteExtensionStartElement = false;
    try {
      BaseShapeHelper.writeStartElement(getElementName(), objectNode, xtw);
      
      writeAdditionalAttributes(objectNode, xtw, modelNode);
      writeAdditionalChildElements(objectNode, xtw, modelNode);
      
      if (didWriteExtensionStartElement) {
      	xtw.writeEndElement();
      }
      
      xtw.writeEndElement();
    
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error writing XML", e);
      throw new XMLException("Error creating BPMN XML from Editor Json", e);
    }
  }
  
  protected String getStencilId(ObjectNode objectNode) {
  	String stencilId = null;
    ObjectNode stencilNode = (ObjectNode) objectNode.get(EDITOR_STENCIL);
    if (stencilNode != null && stencilNode.get(EDITOR_STENCIL_ID) != null) {
      stencilId = stencilNode.get(EDITOR_STENCIL_ID).asText();
    }
    return stencilId;
  }
  
  protected boolean hasProperty(String name, ObjectNode objectNode) {
    boolean hasProperty = false;
    JsonNode propertyNode = getProperty(name, objectNode);
    if (propertyNode != null && StringUtils.isNotEmpty(propertyNode.asText())) {
      hasProperty = true;
    }
    return hasProperty;
  }
  
  protected String getPropertyValueAsString(String name, ObjectNode objectNode) {
    String propertyValue = null;
    JsonNode propertyNode = getProperty(name, objectNode);
    if (propertyNode != null) {
      propertyValue = propertyNode.asText();
    }
    return propertyValue;
  }
  
  protected JsonNode getProperty(String name, ObjectNode objectNode) {
    JsonNode propertyNode = null;
    if (objectNode.get(EDITOR_SHAPE_PROPERTIES) != null) {
      ObjectNode propertiesNode = (ObjectNode) objectNode.get(EDITOR_SHAPE_PROPERTIES);
      propertyNode = propertiesNode.get(name);
    }
    return propertyNode;
  }
  
  protected abstract String getElementName();
  
  protected void writeAdditionalAttributes(ObjectNode objectNode, 
      IndentingXMLStreamWriter xtw, ObjectNode modelNode) throws Exception {}
  
  protected void writeAdditionalChildElements(ObjectNode objectNode, 
      IndentingXMLStreamWriter xtw, ObjectNode modelNode) throws Exception {}
  
  protected void writeAttribute(String attributeName, String propertyName, JsonNode jsonNode, 
      IndentingXMLStreamWriter xtw) throws Exception {
    
    JsonNode node = jsonNode.get(propertyName);
    if (node != null && StringUtils.isNotEmpty(node.getTextValue())) {
      xtw.writeAttribute(attributeName, node.getTextValue());
    }
  }
  
  protected void writeQualifiedAttribute(String attributeName, String value, IndentingXMLStreamWriter xtw) throws Exception {
  	xtw.writeAttribute(ACTIVITI_EXTENSIONS_PREFIX, ACTIVITI_EXTENSIONS_NAMESPACE, attributeName, value);
  }
  
  protected void writeFormProperties(ObjectNode objectNode, IndentingXMLStreamWriter xtw) throws Exception {
    
    JsonNode formPropertiesNode = getProperty(PROPERTY_FORM_PROPERTIES, objectNode);
    if (formPropertiesNode != null) {
      JsonNode itemsArrayNode = formPropertiesNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
      if (itemsArrayNode != null) {
        for (JsonNode formNode : itemsArrayNode) {
          JsonNode formIdNode = formNode.get(PROPERTY_FORM_ID);
          if (formIdNode != null && StringUtils.isNotEmpty(formIdNode.asText())) {
            
            if (didWriteExtensionStartElement == false) { 
              xtw.writeStartElement("extensionElements");
              didWriteExtensionStartElement = true;
            }
            
            xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, "formProperty", ACTIVITI_EXTENSIONS_NAMESPACE);
            xtw.writeAttribute("id", formIdNode.asText());
            
            writeAttribute("name", PROPERTY_FORM_NAME, formNode, xtw);
            writeAttribute("type", PROPERTY_FORM_TYPE, formNode, xtw);
            writeAttribute("expression", PROPERTY_FORM_EXPRESSION, formNode, xtw);
            writeAttribute("variable", PROPERTY_FORM_VARIABLE, formNode, xtw);
            
            xtw.writeEndElement();
          }
        }
      }
    }
  }
  
  protected void writeExecutionListeners(ObjectNode objectNode, IndentingXMLStreamWriter xtw) throws Exception {
  	JsonNode listenersNode = getProperty(PROPERTY_EXECUTION_LISTENERS, objectNode);
    if (listenersNode != null) {
      JsonNode itemsArrayNode = listenersNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
      if (itemsArrayNode != null) {
        for (JsonNode itemNode : itemsArrayNode) {
          JsonNode typeNode = itemNode.get("execution_listener_event_type");
          if (typeNode != null && StringUtils.isNotEmpty(typeNode.asText())) {
            
            if (didWriteExtensionStartElement == false) { 
              xtw.writeStartElement("extensionElements");
              didWriteExtensionStartElement = true;
            }
            
            xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, "executionListener", ACTIVITI_EXTENSIONS_NAMESPACE);
            xtw.writeAttribute("event", typeNode.asText());
            
            writeAttribute("class", "execution_listener_class", itemNode, xtw);
            writeAttribute("expression", "execution_listener_expression", itemNode, xtw);
            writeAttribute("delegateExpression", "execution_listener_delegate_expression", itemNode, xtw);
            
            xtw.writeEndElement();
          }
        }
      }
    }
  }
  
  protected void writeTimerDefinition(ObjectNode objectNode, IndentingXMLStreamWriter xtw) throws Exception {
  	xtw.writeStartElement("timerEventDefinition");
    
    String timeDate = getPropertyValueAsString(PROPERTY_TIMER_DATE, objectNode);
    String timeCycle = getPropertyValueAsString(PROPERTY_TIMER_CYCLE, objectNode);
    String timeDuration = getPropertyValueAsString(PROPERTY_TIMER_DURATON, objectNode);
    
    if (StringUtils.isNotEmpty(timeDate)) {
      xtw.writeStartElement("timeDate");
      xtw.writeCharacters(timeDate);
      xtw.writeEndElement();
      
    } else if (StringUtils.isNotEmpty(timeCycle)) {
      xtw.writeStartElement("timeCycle");
      xtw.writeCharacters(timeCycle);
      xtw.writeEndElement();
      
    } else if (StringUtils.isNotEmpty(timeDuration)) {
      xtw.writeStartElement("timeDuration");
      xtw.writeCharacters(timeDuration);
      xtw.writeEndElement();
    }
    
    xtw.writeEndElement();
  }
  
  protected void writeSignalDefinition(ObjectNode objectNode, IndentingXMLStreamWriter xtw) throws Exception {
  	xtw.writeStartElement("signalEventDefinition");
    
    String signalRef = getPropertyValueAsString(PROPERTY_SIGNALREF, objectNode);
    
    if (StringUtils.isNotEmpty(signalRef)) {
      xtw.writeAttribute("signalRef", signalRef); 
    }
    
    xtw.writeEndElement();
  }
  
  protected void writeMessageDefinition(ObjectNode objectNode, IndentingXMLStreamWriter xtw) throws Exception {
  	xtw.writeStartElement("messageEventDefinition");
    
    String messageRef = getPropertyValueAsString(PROPERTY_MESSAGEREF, objectNode);
    
    if (StringUtils.isNotEmpty(messageRef)) {
      xtw.writeAttribute("messageRef", messageRef); 
    }
    
    xtw.writeEndElement();
  }
  
  protected void writeErrorDefinition(ObjectNode objectNode, IndentingXMLStreamWriter xtw) throws Exception {
  	xtw.writeStartElement("errorEventDefinition");
    
    String errorRef = getPropertyValueAsString(PROPERTY_ERRORREF, objectNode);
    
    if (StringUtils.isNotEmpty(errorRef)) {
      xtw.writeAttribute("errorRef", errorRef); 
    }
    
    xtw.writeEndElement();
  }
}
