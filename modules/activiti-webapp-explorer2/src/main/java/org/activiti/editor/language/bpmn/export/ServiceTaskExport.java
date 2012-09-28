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

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class ServiceTaskExport extends BaseActivityExport {

  protected String getElementName() {
    return "serviceTask";
  }

  protected void writeAdditionalAttributes(ObjectNode objectNode, 
      IndentingXMLStreamWriter xtw, ObjectNode modelNode) throws Exception {
    
  	String className = getPropertyValueAsString(PROPERTY_SERVICETASK_CLASS, objectNode);
  	String expression = getPropertyValueAsString(PROPERTY_SERVICETASK_EXPRESSION, objectNode);
  	String delegateExpression = getPropertyValueAsString(PROPERTY_SERVICETASK_DELEGATE_EXPRESSION, objectNode);
  	
    if (StringUtils.isNotEmpty(className)) {
      writeQualifiedAttribute("class", className, xtw);
    } else if (StringUtils.isNotEmpty(expression)) {
      writeQualifiedAttribute("expression", expression, xtw);
    } else if (StringUtils.isNotEmpty(delegateExpression)) {
      writeQualifiedAttribute("delegateExpression", delegateExpression, xtw);
    }
    
    String resultVariableName = getPropertyValueAsString(PROPERTY_SERVICETASK_RESULT_VARIABLE, objectNode);
    if (StringUtils.isNotEmpty(resultVariableName)) {
    	writeQualifiedAttribute("resultVariable", resultVariableName, xtw);
    }
    
    super.writeAdditionalAttributes(objectNode, xtw, modelNode);
  }
  
  protected void writeAdditionalChildElements(ObjectNode objectNode, 
      IndentingXMLStreamWriter xtw, ObjectNode modelNode) throws Exception {
    
  	JsonNode fieldsNode = getProperty(PROPERTY_SERVICETASK_FIELDS, objectNode);
    if (fieldsNode != null) {
      JsonNode itemsArrayNode = fieldsNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
      if (itemsArrayNode != null) {
        for (JsonNode itemNode : itemsArrayNode) {
          JsonNode nameNode = itemNode.get("servicetask_field_name");
          if (nameNode != null && StringUtils.isNotEmpty(nameNode.asText())) {
            
            if (didWriteExtensionStartElement == false) { 
              xtw.writeStartElement("extensionElements");
              didWriteExtensionStartElement = true;
            }
            
            xtw.writeStartElement(ACTIVITI_EXTENSIONS_PREFIX, "field", ACTIVITI_EXTENSIONS_NAMESPACE);
            xtw.writeAttribute("name", nameNode.asText());
            
            writeAttribute("stringValue", "servicetask_field_value", itemNode, xtw);
            writeAttribute("expression", "servicetask_field_expression", itemNode, xtw);
            
            xtw.writeEndElement();
          }
        }
      }
    }
  }
}
