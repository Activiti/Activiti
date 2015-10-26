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

import org.activiti.engine.ActivitiException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



/**
 * @author Tijs Rademakers
 */
public class LongJsonType extends SerializableType {

  protected final int minLength;
  protected ObjectMapper objectMapper = null;

  public LongJsonType(int minLength, ObjectMapper objectMapper) {
    this.minLength = minLength;
    this.objectMapper = objectMapper;
  }

  public String getTypeName() {
    return "longJson";
  }

  public boolean isAbleToStore(Object value) {
    if (value == null) {
      return true;
    }
    if (JsonNode.class.isAssignableFrom(value.getClass())) {
      JsonNode jsonValue = (JsonNode) value;
      return jsonValue.toString().length() >= minLength;
    }
    return false;
  }
  
  public byte[] serialize(Object value, ValueFields valueFields) {
    if (value == null) {
      return null;
    }
    JsonNode valueNode = (JsonNode) value;
    try {
      return valueNode.toString().getBytes("utf-8");
    } catch (Exception e) {
      throw new ActivitiException("Error getting bytes from json variable", e);
    }
  }
  
  public Object deserialize(byte[] bytes, ValueFields valueFields) {
    JsonNode valueNode = null;
    try {
      valueNode = objectMapper.readTree(bytes);
    } catch (Exception e) {
      throw new ActivitiException("Error reading json variable", e);
    }
    return valueNode;
  }
}
