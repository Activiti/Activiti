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

import org.activiti.editor.json.constants.EditorJsonConstants;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class BaseShapeHelper implements EditorJsonConstants {
  
  public static void writeStartElement(String elementName, ObjectNode objectNode, IndentingXMLStreamWriter xtw) throws Exception {
    xtw.writeStartElement(elementName);
    xtw.writeAttribute("id", objectNode.get(EDITOR_SHAPE_ID).asText());
    if (hasProperty("name", objectNode)) {
      xtw.writeAttribute("name", getPropertyValueAsString("name", objectNode));
    }
  }
  
  public static boolean hasProperty(String name, ObjectNode objectNode) {
    boolean hasProperty = false;
    JsonNode propertyNode = getProperty(name, objectNode);
    if (propertyNode != null && StringUtils.isNotEmpty(propertyNode.asText())) {
      hasProperty = true;
    }
    return hasProperty;
  }
  
  public static String getPropertyValueAsString(String name, ObjectNode objectNode) {
    String propertyValue = null;
    JsonNode propertyNode = getProperty(name, objectNode);
    if (propertyNode != null) {
      propertyValue = propertyNode.asText();
    }
    return propertyValue;
  }
  
  public static JsonNode getProperty(String name, ObjectNode objectNode) {
    JsonNode propertyNode = null;
    if (objectNode.get(EDITOR_SHAPE_PROPERTIES) != null) {
      ObjectNode propertiesNode = (ObjectNode) objectNode.get(EDITOR_SHAPE_PROPERTIES);
      propertyNode = propertiesNode.get(name);
    }
    return propertyNode;
  }
}
