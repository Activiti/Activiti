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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonType implements VariableType {

    private static final Logger logger = LoggerFactory.getLogger(JsonType.class);
    public static final String JSON = "json";

    private final int maxLength;
  private ObjectMapper objectMapper;
  private boolean serializePOJOsInVariablesToJson;
    private JsonTypeConverter jsonTypeConverter;

  public JsonType(int maxLength, ObjectMapper objectMapper, boolean serializePOJOsInVariablesToJson,
      JsonTypeConverter jsonTypeConverter) {
    this.maxLength = maxLength;
    this.objectMapper = objectMapper;
    this.serializePOJOsInVariablesToJson = serializePOJOsInVariablesToJson;
      this.jsonTypeConverter = jsonTypeConverter;
  }

  public String getTypeName() {
    return JSON;
  }

  public boolean isCachable() {
    return true;
  }

  public Object getValue(ValueFields valueFields) {
    Object loadedValue = null;
    if (valueFields.getTextValue() != null && valueFields.getTextValue().length() > 0) {
        try {
            loadedValue = jsonTypeConverter.convertToValue(
                objectMapper.readTree(valueFields.getTextValue()), valueFields);

        } catch (Exception e) {
          logger.error("Error reading json variable " + valueFields.getName(), e);
        }
      }
    return loadedValue;
  }

  public void setValue(Object value, ValueFields valueFields) {
    try {
      valueFields.setTextValue(objectMapper.writeValueAsString(value));
      if (value != null) {
          valueFields.setTextValue2(value.getClass().getName());
      }
    } catch (JsonProcessingException e) {
    logger.error("Error writing json variable " + valueFields.getName(), e);
    }
  }

  public boolean isAbleToStore(Object value) {
    if (value == null) {
      return true;
    }

    if (JsonNode.class.isAssignableFrom(value.getClass()) || (objectMapper.canSerialize(value.getClass()) && serializePOJOsInVariablesToJson)) {
      try {
        return objectMapper.writeValueAsString(value).length() <= maxLength;
      } catch (JsonProcessingException e) {
        logger.error("Error writing json variable of type " + value.getClass(), e);
      }
    }

    return false;
  }

}
