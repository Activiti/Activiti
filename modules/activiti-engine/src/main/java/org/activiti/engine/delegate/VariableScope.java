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

package org.activiti.engine.delegate;

import java.util.Map;
import java.util.Set;

/**
 * @author Tom Baeyens
 */
public interface VariableScope {

  Map<String, Object> getVariables();

  Map<String, Object> getVariablesLocal();

  Object getVariable(String variableName);

  Object getVariableLocal(Object variableName);

  Set<String> getVariableNames();

  Set<String> getVariableNamesLocal();

  void setVariable(String variableName, Object value);

  Object setVariableLocal(String variableName, Object value);

  void setVariables(Map<String, ? extends Object> variables);

  void setVariablesLocal(Map<String, ? extends Object> variables);

  boolean hasVariables();

  boolean hasVariablesLocal();

  boolean hasVariable(String variableName);

  boolean hasVariableLocal(String variableName);

  void createVariableLocal(String variableName, Object value);

  void createVariablesLocal(Map<String, ? extends Object> variables);

  void removeVariable(String variableName);

  void removeVariableLocal(String variableName);

  void removeVariables();

  void removeVariablesLocal();
}