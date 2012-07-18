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

package org.activiti.engine.impl.form;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.VariableScope;


/**
 * Variable-scope only used to resolve variables when NO execution is active but
 * expression-resolving is needed. This occurs when start-form properties have default's
 * defined. Even though variables are not available yet, expressions should be resolved 
 * anyway.
 * 
 * @author Frederik Heremans
 */
public class StartFormVariableScope implements VariableScope {
  
  private static final StartFormVariableScope INSTANCE = new StartFormVariableScope();

  /**
   * Since a {@link StartFormVariableScope} has no state, it's safe to use the same
   * instance to prevent too many useless instances created.
   */
  public static StartFormVariableScope getSharedInstance()  {
    return INSTANCE;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> getVariables() {
    return Collections.EMPTY_MAP;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> getVariablesLocal() {
    return Collections.EMPTY_MAP;
  }

  @Override
  public Object getVariable(String variableName) {
    return null;
  }

  @Override
  public Object getVariableLocal(Object variableName) {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<String> getVariableNames() {
    return Collections.EMPTY_SET;
  }

  @Override
  public Set<String> getVariableNamesLocal() {
    return null;
  }

  @Override
  public void setVariable(String variableName, Object value) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  @Override
  public Object setVariableLocal(String variableName, Object value) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  @Override
  public void setVariables(Map<String, ? extends Object> variables) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  @Override
  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  @Override
  public boolean hasVariables() {
    return false;
  }

  @Override
  public boolean hasVariablesLocal() {
    return false;
  }

  @Override
  public boolean hasVariable(String variableName) {
    return false;
  }

  @Override
  public boolean hasVariableLocal(String variableName) {
    return false;
  }

  @Override
  public void createVariableLocal(String variableName, Object value) {
    throw new UnsupportedOperationException("No execution active, no variables can be created");
  }

  @Override
  public void createVariablesLocal(Map<String, ? extends Object> variables) {
    throw new UnsupportedOperationException("No execution active, no variables can be created");
  }

  @Override
  public void removeVariable(String variableName) {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  @Override
  public void removeVariableLocal(String variableName) {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  @Override
  public void removeVariables() {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  @Override
  public void removeVariablesLocal() {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

}
