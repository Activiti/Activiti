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

import java.util.Date;

import org.activiti.engine.impl.persistence.runtime.VariableInstanceEntity;


/**
 * @author Tom Baeyens
 */
public class DateType implements Type {

  public String getTypeName() {
    return "date";
  }

  public boolean isAbleToStore(Object value) {
    if (value==null) {
      return true;
    }
    return Date.class.isAssignableFrom(value.getClass());
  }

  public Object getValue(VariableInstanceEntity variableInstanceEntity) {
    Long longValue = variableInstanceEntity.getLongValue();
    if (longValue!=null) {
      return new Date(longValue);
    }
    return null;
  }

  public void setValue(Object value, VariableInstanceEntity variableInstanceEntity) {
    if (value!=null) {
      variableInstanceEntity.setLongValue(((Date)value).getTime());
    } else {
      variableInstanceEntity.setLongValue(null);
    }
  }
}
