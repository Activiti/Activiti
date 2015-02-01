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



/**
 * @author Frederik Heremans
 */
public interface RestVariableConverter {

  /**
   * Simple type-name used by this converter.
   */
  String getRestTypeName();
  
  /**
   * Type of variables this converter is able to convert.
   */
  Class<?> getVariableType();
  
  /**
   * Extract the variable value to be used in the engine from the given {@link RestVariable}. 
   */
  Object getVariableValue(RestVariable result);
  
  /**
   * Converts the given value and sets the converted value in the given {@link RestVariable}.
   */
  void convertVariableValue(Object variableValue, RestVariable result);
}
