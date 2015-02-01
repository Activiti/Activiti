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

/**
 * Class that is called when variables need to be rendered. Use these to show a 
 * different representation than the default variable value (toString() call).
 * 
 * @see VariableRendererManager
 * 
 * @author Frederik Heremans
 */
public interface VariableRenderer {

  /**
   * Gets the type this renderer is capable of rendering.
   */
  Class<?> getVariableType();
  
  /**
   * Gets the string-representation of this variable.
   */
  String getStringRepresentation(Object variable);
}
