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

import java.util.UUID;

/**

 */
public class UUIDType implements VariableType {

  private static final long serialVersionUID = 1L;

  public String getTypeName() {
    return "uuid";
  }

  public boolean isCachable() {
    return true;
  }

  public Object getValue(ValueFields valueFields) {
    String textValue = valueFields.getTextValue();
    if (textValue == null)
      return null;
    return UUID.fromString(textValue);
  }

  public void setValue(Object value, ValueFields valueFields) {
    if (value != null) {
      valueFields.setTextValue(value.toString());
    } else {
      valueFields.setTextValue(null);
    }
  }

  public boolean isAbleToStore(Object value) {
    if (value == null) {
      return true;
    }
    return UUID.class.isAssignableFrom(value.getClass());
  }
}