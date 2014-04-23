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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @author zhouyanming (zhouyanming@gmail.com)
 */

@SuppressWarnings({ "rawtypes", "unchecked" })
public class EnumType implements VariableType {
	
  private static Logger log = LoggerFactory.getLogger(EnumType.class);

  public String getTypeName() {
    return "enum";
  }

  public boolean isCachable() {
    return true;
  }

  public Object getValue(ValueFields valueFields) {
		if (valueFields.getTextValue() == null)
			return null;
		try {
			return Enum.valueOf(
					(Class) Class.forName(valueFields.getTextValue2()),
					valueFields.getTextValue());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
  }

  public void setValue(Object value, ValueFields valueFields) {
	if (value == null) {
		valueFields.setTextValue(null);
	} else {
		Enum en = (Enum) value;
		valueFields.setTextValue(en.name());
		valueFields.setTextValue2(en.getClass().getName());
	}
  }

  public boolean isAbleToStore(Object value) {
	  if (value == null) {
	      return true;
	    }
	  return value instanceof Enum;
  }
}
