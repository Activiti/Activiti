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

import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.persistence.entity.VariableInstance;

/**
 * Interface for class that acts as a scope for variables: i.e. the implementation
 * can be used to set and get variables.
 * 
 * Typically, executions (and thus process instances) and tasks are the primary use case
 * to get and set variables. The {@link DelegateExecution} for example is often used
 * in {@link JavaDelegate} implementation to get and set variables.
 * 
 * Variables are typically stored on the 'highest parent'. For executions, this
 * means that when called on an execution the variable will be stored on the process instance
 * execution. Variables can be stored on the actual scope itself though, by calling the xxLocal methods.
 * 


 */
public interface VariableScope {

  /**
   * Returns all variables. 
   * This will include all variables of parent scopes too. 
   */
  Map<String, Object> getVariables();
  
  /**
   * Returns all variables, as instances of the {@link VariableInstance} interface,
   * which gives more information than only the the value (type, execution id, etc.)
   */
  Map<String, VariableInstance> getVariableInstances();
  
  /**
   * Similar to {@link #getVariables()}, but limited to only the variables with the provided names.
   */
  Map<String, Object> getVariables(Collection<String> variableNames);
  
  /**
   * Similar to {@link #getVariableInstances()}, but limited to only the variables with the provided names.
   */
  Map<String, VariableInstance> getVariableInstances(Collection<String> variableNames);

  /**
   * Similar to {@link #getVariables(Collection))}, but with a flag that indicates that all 
   * variables should be fetched when fetching the specific variables.
   *  
   * If set to false, only the specific variables will be fetched.
   * Dependening on the use case, this can be better for performance, as it avoids fetching and processing 
   * the other variables. However, if the other variables are needed further on, getting them in
   * one go is probably better (and the variables are cached during one {@link Command} execution).
   */
  Map<String, Object> getVariables(Collection<String> variableNames, boolean fetchAllVariables);
  
  /**
   * Similar to {@link #getVariables(Collection, boolean)} but returns the variables 
   * as instances of the {@link VariableInstance} interface,
   * which gives more information than only the the value (type, execution id, etc.)
   */
  Map<String, VariableInstance> getVariableInstances(Collection<String> variableNames, boolean fetchAllVariables);

  /**
   * Returns the variable local to this scope only.
   * So, in contrary to {@link #getVariables()}, the variables from the parent scope won't be returned. 
   */
  Map<String, Object> getVariablesLocal();
  
  /**
   * Returns the variables local to this scope as instances of the {@link VariableInstance} interface,
   * which provided additional information about the variable.
   */
  Map<String, VariableInstance> getVariableInstancesLocal();

  /**
   * Similar to {@link #getVariables(Collection)}, but only for variables local to this scope.
   */
  Map<String, Object> getVariablesLocal(Collection<String> variableNames);
  
  /**
   * Similar to {@link #getVariableInstances(Collection)}, but only for variables local to this scope.
   */
  Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> variableNames);

  /**
   * Similar to {@link #getVariables(Collection, boolean)}, but only for variables local to this scope.
   */
  Map<String, Object> getVariablesLocal(Collection<String> variableNames, boolean fetchAllVariables);
  
  /**
   * Similar to {@link #getVariableInstances(Collection, boolean)}, but only for variables local to this scope.
   */
  Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> variableNames, boolean fetchAllVariables);

  /**
   * Returns the variable value for one specific variable.
   * Will look in parent scopes when the variable does not exist on this particular scope. 
   */
  Object getVariable(String variableName);
  
  /**
   * Similar to {@link #getVariable(String)}, but returns a {@link VariableInstance} instance,
   * which contains more information than just the value. 
   */
  VariableInstance getVariableInstance(String variableName);

  /**
   * Similar to {@link #getVariable(String)}, but has an extra flag that indicates whether or not 
   * all variables need to be fetched when getting one variable.
   * 
   * By default true (for backwards compatibility reasons), which means that calling {@link #getVariable(String)}
   * will fetch all variables, of the current scope and all parent scopes.
   * Setting this flag to false can thus be better for performance. However, variables are cached, and 
   * if other variables are used later on, setting this true might actually be better for performance.
   */
  Object getVariable(String variableName, boolean fetchAllVariables);
  
  /**
   * Similar to {@link #getVariable(String, boolean)}, but returns an instance of {@link VariableInstance}, 
   * which has some additional information beyond the value. 
   */
  VariableInstance getVariableInstance(String variableName, boolean fetchAllVariables);

  /**
   * Returns the value for the specific variable and only checks this scope and not any parent scope.
   */
  Object getVariableLocal(String variableName);
  
  /**
   * Similar to {@link #getVariableLocal(String)}, but returns an instance of {@link VariableInstance}, 
   * which has some additional information beyond the value. 
   */
  VariableInstance getVariableInstanceLocal(String variableName);

  /**
   * Similar to {@link #getVariableLocal(String)}, but has an extra flag that indicates whether or not 
   * all variables need to be fetched when getting one variable.
   * 
   * By default true (for backwards compatibility reasons), which means that calling {@link #getVariableLocal(String)}
   * will fetch all variables, of the current scope.
   * Setting this flag to false can thus be better for performance. However, variables are cached, and 
   * if other variables are used later on, setting this true might actually be better for performance.
   */
  Object getVariableLocal(String variableName, boolean fetchAllVariables);
  
  /**
   * Similar to {@link #getVariableLocal(String, boolean)}, but returns an instance of {@link VariableInstance}, 
   * which has some additional information beyond the value.
   */
  VariableInstance getVariableInstanceLocal(String variableName, boolean fetchAllVariables);

  /**
   * Typed version of the {@link #getVariable(String)} method. 
   */
  <T> T getVariable(String variableName, Class<T> variableClass);

  /**
   * Typed version of the {@link #getVariableLocal(String)} method.
   */
  <T> T getVariableLocal(String variableName, Class<T> variableClass);

  /**
   * Returns all the names of the variables for this scope and all parent scopes. 
   */
  Set<String> getVariableNames();

  /**
   * Returns all the names of the variables for this scope (no parent scopes).
   */
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

  /**
   * Similar to {@link #setVariable(String, Object)}, but with an extra flag to indicate whether 
   * all variables should be fetched while doing this or not.
   * 
   * The variable will be put on the highest possible scope. For an execution this is the process instance execution.
   * If this is not wanted, use the {@link #setVariableLocal(String, Object)} method instead. 
   * 
   * The default (e.g. when calling {@link #setVariable(String, Object)}), is <i>true</i>, for backwards
   * compatibility reasons. However, in some use cases, it might make sense not to fetch any other variables
   * when setting one variable (for example when doing nothing more than just setting one variable).
   */
  void setVariable(String variableName, Object value, boolean fetchAllVariables);

  /**
   * Similar to {@link #setVariable(String, Object)}, but the variable is set to this scope specifically.
   */
  Object setVariableLocal(String variableName, Object value);

  /**
   * Similar to {@link #setVariableLocal(String, Object, boolean)}, but the variable is set to this scope specifically. 
   */
  Object setVariableLocal(String variableName, Object value, boolean fetchAllVariables);

  /**
   * Sets the provided variables to the variable scope.
   * 
   * <p>
   * Variables are set according algorithm for {@link #setVariable(String, Object)}, applied separately to each variable.
   * 
   * @param variables
   *          a map of keys and values for the variables to be set
   */
  void setVariables(Map<String, ? extends Object> variables);

  /**
   * Similar to {@link #setVariables(Map)}, but the variable are set on this scope specifically.
   */
  void setVariablesLocal(Map<String, ? extends Object> variables);

  /**
   * Returns whether this scope or any parent scope has variables. 
   */
  boolean hasVariables();

  /**
   * Returns whether this scope has variables. 
   */
  boolean hasVariablesLocal();

  /**
   * Returns whether this scope or any parent scope has a specific variable. 
   */
  boolean hasVariable(String variableName);

  /**
   * Returns whether this scope has a specific variable. 
   */
  boolean hasVariableLocal(String variableName);

  /**
   * Removes the variable and creates a new;@link HistoricVariableUpdateEntity}
   */
  void removeVariable(String variableName);

  /**
   * Removes the local variable and creates a new {@link HistoricVariableUpdate}.
   */
  void removeVariableLocal(String variableName);

  /**
   * Removes the variables and creates a new
   * {@link HistoricVariableUpdate} for each of them.
   */
  void removeVariables(Collection<String> variableNames);

  /**
   * Removes the local variables and creates a new
   * {@link HistoricVariableUpdate} for each of them.
   */
  void removeVariablesLocal(Collection<String> variableNames);

  /**
   * Removes the (local) variables and creates a new
   * {@link HistoricVariableUpdate} for each of them.
   */
  void removeVariables();

  /**
   * Removes the (local) variables and creates a new
   * {@link HistoricVariableUpdate} for each of them.
   */
  void removeVariablesLocal();
  
  /**
   * Similar to {@link #setVariable(String, Object)}, but the variable is transient:
   * 
   * - no history is kept for the variable
   * - the variable is only available until a waitstate is reached in the process
   * - transient variables 'shadow' persistent variable (when getVariable('abc') 
   *   where 'abc' is both persistent and transient, the transient value is returned.
   */
  void setTransientVariable(String variableName, Object variableValue);
  
  /**
   * Similar to {@link #setVariableLocal(String, Object)}, but for a transient variable.
   * See {@link #setTransientVariable(String, Object)} for the rules on 'transient' variables. 
   */
  void setTransientVariableLocal(String variableName, Object variableValue);

  /**
   * Similar to {@link #setVariables(Map)}, but for transient variables.
   * See {@link #setTransientVariable(String, Object)} for the rules on 'transient' variables. 
   */
  void setTransientVariables(Map<String, Object> transientVariables);

  /**
   * Similar to {@link #getVariable(String)}, including the searching via the parent scopes, but
   * for transient variables only.
   * See {@link #setTransientVariable(String, Object)} for the rules on 'transient' variables.
   */
  Object getTransientVariable(String variableName);

  /**
   * Similar to {@link #getVariables()}, but for transient variables only.
   * See {@link #setTransientVariable(String, Object)} for the rules on 'transient' variables.
   */
  Map<String, Object> getTransientVariables();
  
  /**
   * Similar to {@link #setVariablesLocal(Map)}, but for transient variables.
   * See {@link #setTransientVariable(String, Object)} for the rules on 'transient' variables. 
   */
  void setTransientVariablesLocal(Map<String, Object> transientVariables);

  /**
   * Similar to {@link #getVariableLocal(String)}, but for a transient variable.
   * See {@link #setTransientVariable(String, Object)} for the rules on 'transient' variables. 
   */
  Object getTransientVariableLocal(String variableName);
  
  /**
   * Similar to {@link #getVariableLocal(String)}, but for transient variables only.
   * See {@link #setTransientVariable(String, Object)} for the rules on 'transient' variables.
   */
  Map<String, Object> getTransientVariablesLocal();

  /**
   * Removes a specific transient variable (also searching parent scopes).
   * See {@link #setTransientVariable(String, Object)} for the rules on 'transient' variables. 
   */
  void removeTransientVariableLocal(String variableName);
  
  /**
   * Removes a specific transient variable.
   * See {@link #setTransientVariable(String, Object)} for the rules on 'transient' variables. 
   */
  void removeTransientVariable(String variableName);
  
  /**
   * Remove all transient variable of this scope and its parent scopes.
   * See {@link #setTransientVariable(String, Object)} for the rules on 'transient' variables.
   */
  void removeTransientVariables();

  /**
   * Removes all local transient variables.
   * See {@link #setTransientVariable(String, Object)} for the rules on 'transient' variables.
   */
  void removeTransientVariablesLocal();
  
}