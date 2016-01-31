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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.persistence.entity.VariableInstance;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface VariableScope {

  Map<String, Object> getVariables();
  
  Map<String, VariableInstance> getVariableInstances();
  
  Map<String, Object> getVariables(Collection<String> variableNames);
  
  Map<String, VariableInstance> getVariableInstances(Collection<String> variableNames);
  
  Map<String, Object> getVariables(Collection<String> variableNames, boolean fetchAllVariables);
  
  Map<String, VariableInstance> getVariableInstances(Collection<String> variableNames, boolean fetchAllVariables);

  Map<String, Object> getVariablesLocal();
  
  Map<String, VariableInstance> getVariableInstancesLocal();
  
  Map<String, Object> getVariablesLocal(Collection<String> variableNames);
  
  Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> variableNames);
  
  Map<String, Object> getVariablesLocal(Collection<String> variableNames, boolean fetchAllVariables);
  
  Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> variableNames, boolean fetchAllVariables);
  
  Object getVariable(String variableName);
  
  VariableInstance getVariableInstance(String variableName);
  
  Object getVariable(String variableName, boolean fetchAllVariables);
  
  VariableInstance getVariableInstance(String variableName, boolean fetchAllVariables);

  Object getVariableLocal(String variableName);
  
  VariableInstance getVariableInstanceLocal(String variableName);
  
  Object getVariableLocal(String variableName, boolean fetchAllVariables);
  
  VariableInstance getVariableInstanceLocal(String variableName, boolean fetchAllVariables);

  <T> T getVariable(String variableName, Class<T> variableClass);

  <T> T getVariableLocal(String variableName, Class<T> variableClass);

  Set<String> getVariableNames();

  Set<String> getVariableNamesLocal();

  /**
   * Sets the variable with the provided name to the provided value.
   * 
   * <p>
   * A variable is set according to the following algorithm:
   * 
   * <p>
   * <li>If this scope already contains a variable by the provided name as a
   * <strong>local</strong> variable, its value is overwritten to the provided
   * value.</li>
   * <li>If this scope does <strong>not</strong> contain a variable by the
   * provided name as a local variable, the variable is set to this scope's
   * parent scope, if there is one. If there is no parent scope (meaning this
   * scope is the root scope of the hierarchy it belongs to), this scope is
   * used. This applies recursively up the parent scope chain until, if no scope
   * contains a local variable by the provided name, ultimately the root scope
   * is reached and the variable value is set on that scope.</li>
   * <p>
   * In practice for most cases, this algorithm will set variables to the scope
   * of the execution at the process instance’s root level, if there is no
   * execution-local variable by the provided name.
   * 
   * @param variableName
   *          the name of the variable to be set
   * @param value
   *          the value of the variable to be set
   */
  void setVariable(String variableName, Object value);
  
  void setVariable(String variableName, Object value, boolean fetchAllVariables);

  Object setVariableLocal(String variableName, Object value);
  
  Object setVariableLocal(String variableName, Object value, boolean fetchAllVariables);

  /**
   * Sets the provided variables to the variable scope.
   * 
   * <p>
   * Variables are set according algorithm for
   * {@link #setVariable(String, Object)}, applied separately to each variable.
   * 
   * @see #setVariable(String, Object)
   *      {@link VariableScope#setVariable(String, Object)}
   * 
   * @param variables
   *          a map of keys and values for the variables to be set
   */
  void setVariables(Map<String, ? extends Object> variables);
  
  void setVariablesLocal(Map<String, ? extends Object> variables);
  
  boolean hasVariables();

  boolean hasVariablesLocal();

  boolean hasVariable(String variableName);

  boolean hasVariableLocal(String variableName);

  void createVariableLocal(String variableName, Object value);

  /**
   * Removes the variable and creates a new {@link HistoricVariableUpdateEntity}
   * .
   */
  void removeVariable(String variableName);

  /**
   * Removes the local variable and creates a new
   * {@link HistoricVariableUpdateEntity}.
   */
  void removeVariableLocal(String variableName);

  /**
   * Removes the variables and creates a new
   * {@link HistoricVariableUpdateEntity} for each of them.
   */
  void removeVariables(Collection<String> variableNames);

  /**
   * Removes the local variables and creates a new
   * {@link HistoricVariableUpdateEntity} for each of them.
   */
  void removeVariablesLocal(Collection<String> variableNames);

  /**
   * Removes the (local) variables and creates a new
   * {@link HistoricVariableUpdateEntity} for each of them.
   */
  void removeVariables();

  /**
   * Removes the (local) variables and creates a new
   * {@link HistoricVariableUpdateEntity} for each of them.
   */
  void removeVariablesLocal();

}