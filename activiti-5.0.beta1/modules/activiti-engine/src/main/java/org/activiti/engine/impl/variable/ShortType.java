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

import org.activiti.engine.impl.runtime.VariableInstanceEntity;


/**
 * @author Joram Barrez
 */
public class ShortType implements Type {

  private static final long serialVersionUID = 1L;
  
  public String getTypeName() {
    return "short";
  }

  public Object getValue(VariableInstanceEntity variableInstanceEntity) {
    return new Short(variableInstanceEntity.getLongValue().shortValue());
  }

  public void setValue(Object value, VariableInstanceEntity variableInstanceEntity) {
    variableInstanceEntity.setLongValue(((Short) value).longValue());
    if (value!=null) {
      variableInstanceEntity.setTextValue(value.toString());
    } else {
      variableInstanceEntity.setTextValue(null);
    }
  }

  public boolean isAbleToStore(Object value) {
    if (value==null) {
      return true;
    }
    return Short.class.isAssignableFrom(value.getClass())
           || short.class.isAssignableFrom(value.getClass());
  }
}
