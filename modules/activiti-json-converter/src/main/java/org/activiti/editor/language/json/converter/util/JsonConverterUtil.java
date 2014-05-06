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
    
    if (PROPERTY_VALUE_YES.equalsIgnoreCase(stringValue)) {
      result = true;
    } else if (PROPERTY_VALUE_NO.equalsIgnoreCase(stringValue)) {
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
