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
import org.activiti.engine.ActivitiException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**

 */
public class LongJsonType extends SerializableType {

  private static final Logger logger = LoggerFactory.getLogger(LongJsonType.class);

  protected final int minLength;
  protected ObjectMapper objectMapper;
  protected boolean serializePOJOsInVariablesToJson;
  protected String javaClassFieldForJackson;

  public LongJsonType(int minLength, ObjectMapper objectMapper,boolean serializePOJOsInVariablesToJson, String javaClassFieldForJackson) {
    this.minLength = minLength;
    this.objectMapper = objectMapper;
    this.serializePOJOsInVariablesToJson = serializePOJOsInVariablesToJson;
    this.javaClassFieldForJackson = javaClassFieldForJackson;
  }

  public String getTypeName() {
    return "longJson";
  }

  public boolean isAbleToStore(Object value) {
    if (value == null) {
      return true;
    }

    if(JsonNode.class.isAssignableFrom(value.getClass()) || (objectMapper.canSerialize(value.getClass()) && serializePOJOsInVariablesToJson)){
      try {
        return objectMapper.writeValueAsString(value).length()>= minLength;
      } catch (JsonProcessingException e) {
        logger.error("Error writing json variable of type " +value.getClass(), e);
      }
    }

    return false;
  }


  public byte[] serialize(Object value, ValueFields valueFields) {
    if (value == null) {
      return null;
    }
    String json = null;
    try{
      json = objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      logger.error("Error writing long json variable " + valueFields.getName(), e);
    }
    try {
      return json.getBytes("utf-8");
    } catch (Exception e) {
      throw new ActivitiException("Error getting bytes from json variable", e);
    }
  }

  public Object deserialize(byte[] bytes, ValueFields valueFields) {
    Object jsonValue = null;
    if(jsonValue==null) {
      try {
        jsonValue = objectMapper.readTree(bytes);
      } catch (Exception e) {
        logger.error("Error reading json variable " + valueFields.getName(), e);
      }
    }
    if(jsonValue!=null && StringUtils.isNotBlank(javaClassFieldForJackson) ) {
      //can find type so long as JsonTypeInfo annotation on the class - see https://stackoverflow.com/a/28384407/9705485
      JsonNode classNode = ((JsonNode)jsonValue).get(javaClassFieldForJackson);
      if(classNode != null) {
        final String type = classNode.asText();
        Class<?> cls = null;
        try {
          cls = Class.forName(type, false, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
          logger.warn("Unable to obtain type for json variable object " + valueFields.getName(), e);
        }
        if(cls!=null) {
          jsonValue = objectMapper.convertValue(jsonValue, cls);
        }
      }
    }
    return jsonValue;
  }
}
