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
package org.activiti.engine.impl.variable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



/**

 */
public class JsonType implements VariableType {
  
  private static final Logger logger = LoggerFactory.getLogger(JsonType.class);
  
  protected final int maxLength;
  protected ObjectMapper objectMapper;

  public JsonType(int maxLength, ObjectMapper objectMapper) {
    this.maxLength = maxLength;
    this.objectMapper = objectMapper;
  }

  public String getTypeName() {
    return "json";
  }

  public boolean isCachable() {
    return true;
  }

  public Object getValue(ValueFields valueFields) {
    JsonNode jsonValue = null;
    if (valueFields.getTextValue() != null && valueFields.getTextValue().length() > 0) {
      try {
        jsonValue = objectMapper.readTree(valueFields.getTextValue());

      } catch (Exception e) {
        logger.error("Error reading json variable " + valueFields.getName(), e);
      }
    }
    if (jsonValue!=null){
      String type = getObjectTypeFromJson(jsonValue);
      if (type!=null){
        return readObjectFromJson(jsonValue,type);
      }
    }
    return jsonValue;
  }

  public Object readObjectFromJson(JsonNode jsonNode, String type){
      Class<?> cls = null;
      try {
        cls = Class.forName(type, false, this.getClass().getClassLoader());
      } catch (ClassNotFoundException e) {
        logger.error("Error reading object from json " + jsonNode, e);
      }

      return objectMapper.convertValue(jsonNode, cls);
  }

  public String getObjectTypeFromJson(JsonNode jsonNode) {
    return jsonNode.get("type") != null ? jsonNode.get("type").asText() : null;
  }

  public void setValue(Object value, ValueFields valueFields) {
    if(includesTypeInfoForDeserliaizing(value)){
      try {
        valueFields.setTextValue(value != null ? objectMapper.writeValueAsString(value) : null);
      } catch (JsonProcessingException e) {
        logger.error("Error writing json variable " + valueFields.getName(), e);
      }
    } else {
      valueFields.setTextValue(value != null ? value.toString() : null);
    }
  }

  public boolean isAbleToStore(Object value) {
    if (value == null) {
      return true;
    }
    if (JsonNode.class.isAssignableFrom(value.getClass())) {
      JsonNode jsonValue = (JsonNode) value;
      return jsonValue.toString().length() <= maxLength;
    }

    if(includesTypeInfoForDeserliaizing(value)){
      try {
        String jsonValue = objectMapper.writeValueAsString(value);
        return jsonValue.toString().length() <= maxLength;
      } catch (JsonProcessingException e) {
        logger.error("Error reading object as json variable "+value, e);
        return false;
      }

    }

    return false;
  }

  public boolean includesTypeInfoForDeserliaizing(Object value){
    return value.getClass().isAnnotationPresent(JsonTypeInfo.class) &&
            value.getClass().getAnnotation(JsonTypeInfo.class).property().equals("type") &&
            value.getClass().getAnnotation(JsonTypeInfo.class).include().equals(JsonTypeInfo.As.PROPERTY) &&
            value.getClass().getAnnotation(JsonTypeInfo.class).use().equals(JsonTypeInfo.Id.CLASS);
  }
}
