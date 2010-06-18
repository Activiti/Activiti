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
package org.activiti.impl.variable;


/**
 * @author Tom Baeyens
 */
public class LongVariableInstance extends VariableInstance {

  private static final long serialVersionUID = 1L;
  
  protected Long value;

  public String getTypeName() {
    return "long";
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = (Long) value;
    if (value!=null) {
      this.text = value.toString();
    } else {
      this.text = null;
    }
  }

  public boolean isAbleToStore(Object value) {
    if (value==null) {
      return true;
    }
    return Long.class.isAssignableFrom(value.getClass())
           || long.class.isAssignableFrom(value.getClass());
  }
}
