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

package org.activiti.explorer.ui.variable;

import java.util.HashMap;
import java.util.Map;


/**
 * Manager responsible for rendering variable values.
 * 
 * @author Frederik Heremans
 */
public class VariableRendererManager {

  private Map<Class<?>, VariableRenderer> renderers = new HashMap<Class<?>, VariableRenderer>();
  
  /**
   * Add variable renderer. If a renderer for the same type is already registered,
   * it will be replaced with this one.
   */
  public void addVariableRenderer(VariableRenderer renderer) {
    renderers.put(renderer.getVariableType(), renderer);
  }
  
  /**
   * Get the renderer for the given type. Returns null, when no renderer is found.
   */
  public VariableRenderer getVariableRenderer(Class<?> variableType) {
    return renderers.get(variableType);
  }

  /**
   * Get the string-representation to use. When no renderer is found for the
   * type of variable, toString() is called.
   */
  public String getStringRepresentation(Object variableValue) {
    if(variableValue != null) {
      final VariableRenderer renderer = getVariableRenderer(variableValue.getClass());
      if(renderer != null) {
        return renderer.getStringRepresentation(variableValue);
      } else {
        return variableValue.toString();
      }
    }
    return null;
  }
}
