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

import org.activiti.engine.impl.util.json.JSONObject;


/**
 * @author Tom Baeyens
 */
public abstract class JsonObjectConverter <T> {

  public void toJson(T object, Writer writer) {
    toJsonObject(object).write(writer);
  }
  
  public String toJson(T object) {
    return toJsonObject(object).toString();
  }
  
  public String toJson(T object, int indentFactor) {
    return toJsonObject(object).toString(indentFactor);
  }
  
  public abstract JSONObject toJsonObject(T object);
  public abstract T toObject(Reader reader);
}
