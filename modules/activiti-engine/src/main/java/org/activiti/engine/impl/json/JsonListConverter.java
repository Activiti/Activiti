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
package org.activiti.engine.impl.json;

import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.json.JSONArray;


/**
 * @author Tom Baeyens
 */
public class JsonListConverter<T> {

  JsonObjectConverter<T> jsonObjectConverter;
  
  public JsonListConverter(JsonObjectConverter<T> jsonObjectConverter) {
    this.jsonObjectConverter = jsonObjectConverter;
  }

  public void toJson(List<T> list, Writer writer) {
    toJsonArray(list).write(writer);
  }

  public String toJson(List<T> list) {
    return toJsonArray(list).toString();
  }
  
  public String toJson(List<T> list, int indentFactor) {
    return toJsonArray(list).toString(indentFactor);
  }
  
  private JSONArray toJsonArray(List<T> objects) {
    JSONArray jsonArray = new JSONArray();
    for (T object: objects) {
      jsonArray.put(jsonObjectConverter.toJsonObject(object));
    }
    return jsonArray;
  }

  public List<T> toObject(Reader reader) {
    throw new ActivitiException("not yet implemented");
  }
  
  public JsonObjectConverter<T> getJsonObjectConverter() {
    return jsonObjectConverter;
  }
  public void setJsonObjectConverter(JsonObjectConverter<T> jsonObjectConverter) {
    this.jsonObjectConverter = jsonObjectConverter;
  }
}
