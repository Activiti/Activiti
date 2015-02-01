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

package org.activiti.engine.impl.el;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;

/**
 * Expression that always returns the same value when <code>getValue</code> is
 * called. Setting of the value is not supported.
 * 
 * @author Frederik Heremans
 */
public class FixedValue implements Expression {

  private static final long serialVersionUID = 1L;
  private Object value;

  public FixedValue(Object value) {
    this.value = value;
  }

  public Object getValue(VariableScope variableScope) {
    return value;
  }
  
  public void setValue(Object value, VariableScope variableScope) {
    throw new ActivitiException("Cannot change fixed value");
  }
  
  public String getExpressionText() {
    return value.toString();
  }

}
