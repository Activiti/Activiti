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
 * @author Joram Barrez
 */
public class IntegerType implements Type {

  private static final long serialVersionUID = 1L;
  
  public String getTypeName() {
    return "integer";
  }

  public Object getValue(VariableInstance variableInstance) {
    return new Integer(variableInstance.getLongValue().intValue());
  }

  public void setValue(Object value, VariableInstance variableInstance) {
    variableInstance.setLongValue(((Integer) value).longValue());
    if (value!=null) {
      variableInstance.setTextValue(value.toString());
    } else {
      variableInstance.setTextValue(null);
    }
  }

  public boolean isAbleToStore(Object value) {
    if (value==null) {
      return true;
    }
    return Integer.class.isAssignableFrom(value.getClass())
           || int.class.isAssignableFrom(value.getClass());
  }
}
