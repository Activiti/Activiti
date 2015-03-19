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
package org.activiti.editor.language.json.converter.util;

import java.util.ArrayList;
import java.util.List;

import org.activiti.editor.constants.EditorJsonConstants;
import org.activiti.editor.constants.StencilConstants;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonConverterUtil implements EditorJsonConstants, StencilConstants {
  
  public static String getPropertyValueAsString(String name, JsonNode objectNode) {
    String propertyValue = null;
    JsonNode propertyNode = getProperty(name, objectNode);
    if (propertyNode != null && "null".equalsIgnoreCase(propertyNode.asText()) == false) {
      propertyValue = propertyNode.asText();
    }
    return propertyValue;
  }
  
  public static boolean getPropertyValueAsBoolean(String name, JsonNode objectNode) {
    return getPropertyValueAsBoolean(name, objectNode, false);
  }
  
  public static boolean getPropertyValueAsBoolean(String name, JsonNode objectNode, boolean defaultValue) {
    boolean result = defaultValue;
    String stringValue = getPropertyValueAsString(name, objectNode);
    
    if (PROPERTY_VALUE_YES.equalsIgnoreCase(stringValue) || "true".equalsIgnoreCase(stringValue)) {
      result = true;
    } else if (PROPERTY_VALUE_NO.equalsIgnoreCase(stringValue) || "false".equalsIgnoreCase(stringValue)) {
      result = false;
    }
    
    return result;
  }
  
  public static List<String> getPropertyValueAsList(String name, JsonNode objectNode) {
    List<String> resultList = new ArrayList<String>();
    JsonNode propertyNode = getProperty(name, objectNode);
    if (propertyNode != null && "null".equalsIgnoreCase(propertyNode.asText()) == false) {
      String propertyValue = propertyNode.asText();
      String[] valueList = propertyValue.split(",");
      for (String value : valueList) {
        resultList.add(value.trim());
      }
    }
    return resultList;
  }
  
  public static JsonNode getProperty(String name, JsonNode objectNode) {
    JsonNode propertyNode = null;
    if (objectNode.get(EDITOR_SHAPE_PROPERTIES) != null) {
      JsonNode propertiesNode = objectNode.get(EDITOR_SHAPE_PROPERTIES);
      propertyNode = propertiesNode.get(name);
    }
    return propertyNode;
  }

}
