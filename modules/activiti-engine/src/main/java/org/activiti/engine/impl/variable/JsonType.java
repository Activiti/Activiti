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


import org.activiti.engine.impl.persistence.entity.json.JsonVariableScopeImpl;

import javax.json.*;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author Tim Stephenson
 */
public class JsonType extends StringType implements VariableType {

  public JsonType(int maxLength) {super(maxLength);}

  public String getTypeName() {
    return "json";
  }

  public Object getValue(ValueFields valueFields) {
    return parse(valueFields.getTextValue());
  }

  public void setValue(Object value, ValueFields valueFields) {
    valueFields.setTextValue(stringify((JsonStructure) value));
    valueFields.setCachedValue(valueFields.getTextValue());
  }

  public boolean isAbleToStore(Object value) {
    if (value instanceof JsonStructure || (super.isAbleToStore(value) && appearsToBeJson(value))) {
      return true;
    }
    return false;
  }

  public static JsonObject toObject(String value) {
    JsonReader jsonReader = Json.createReader(new StringReader((String) value));
    return jsonReader.readObject();
  }


  public static JsonArray toArray(String value) {
    JsonReader jsonReader = Json.createReader(new StringReader((String) value));
    return jsonReader.readArray();
  }

  public static boolean appearsToBeJson(Object value) {
    return value instanceof String && (value.toString().trim().startsWith("{") || value.toString().trim().startsWith("["));
  }

  public static JsonStructure parse(String value) {
    return value.trim().startsWith("[") ? toArray(value) : toObject(value);
  }

  public String stringify(JsonStructure value) {
    StringWriter sw = new StringWriter();
    JsonWriter jsonWriter = Json.createWriter(sw);
    jsonWriter.write(value);
    jsonWriter.close();
    return sw.toString();
  }
}
