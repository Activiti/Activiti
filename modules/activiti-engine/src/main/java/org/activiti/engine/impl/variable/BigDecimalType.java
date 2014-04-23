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

import java.math.BigDecimal;



/**
 * @author zhouyanming (zhouyanming@gmail.com)
 */
public class BigDecimalType implements VariableType {

  public String getTypeName() {
    return "bigDecimal";
  }

  public boolean isCachable() {
    return true;
  }

  public Object getValue(ValueFields valueFields) {
	return valueFields.getDoubleValue() != null ? new BigDecimal(
				valueFields.getDoubleValue()) : null;
  }

  public void setValue(Object value, ValueFields valueFields) {
	if (value == null) {
		valueFields.setDoubleValue(null);
	} else {
		valueFields.setDoubleValue(((BigDecimal) value).doubleValue());
	}
  }

  public boolean isAbleToStore(Object value) {
    if (value == null) {
      return true;
    }
    return BigDecimal.class.isAssignableFrom(value.getClass());
  }
}
