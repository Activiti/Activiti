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

package org.activiti.rest.service.api.engine.variable;

import org.activiti.engine.ActivitiIllegalArgumentException;


/**
 * @author Frederik Heremans
 */
public class LongRestVariableConverter implements RestVariableConverter {

  @Override
  public String getRestTypeName() {
    return "long";
  }

  @Override
  public Class< ? > getVariableType() {
    return Long.class;
  }

  @Override
  public Object getVariableValue(RestVariable result) {
    if(result.getValue() != null) {
      if(!(result.getValue() instanceof Number)) {
        throw new ActivitiIllegalArgumentException("Converter can only convert longs");
      }
      return ((Number) result.getValue()).longValue();
    }
    return null;
  }

  @Override
  public void convertVariableValue(Object variableValue, RestVariable result) {
    if(variableValue != null) {
      if(!(variableValue instanceof Long)) {
        throw new ActivitiIllegalArgumentException("Converter can only convert integers");
      }
      result.setValue(variableValue);
    } else {
      result.setValue(null);
    }
  }

}
