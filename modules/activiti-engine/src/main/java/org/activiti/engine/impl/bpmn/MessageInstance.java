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
package org.activiti.engine.impl.bpmn;

import java.util.HashMap;
import java.util.Map;

/**
 * An instance of a {@link Message}
 * 
 * @author Esteban Robles Luna
 */
public class MessageInstance {

  protected Message message;
  
  protected Map<String, Object> fieldValues;
  
  MessageInstance(Message message) {
    this.message = message;
    this.fieldValues = new HashMap<String, Object>();
  }
  
  public Object getFieldValue(String fieldName) {
    return this.fieldValues.get(fieldName);
  }
  
  public void setFieldValue(String fieldName, Object value) {
    this.fieldValues.put(fieldName, value);
  }
  
  public int getFieldSize() {
    return this.getMessage().getStructure().getFieldSize();
  }
  
  public String getFieldNameAt(int index) {
    return this.getMessage().getStructure().getFieldNameAt(index);
  }
  
  public Message getMessage() {
    return this.message;
  }
}
