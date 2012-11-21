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

import org.springframework.beans.factory.InitializingBean;

/**
 * Simple class that registers the {@link VariableRenderer} configured in it
 * with the {@link VariableRendererManager} when bean is created.
 * 
 * @author Frederik Heremans
 */
public class VariableRendererConfigurer implements InitializingBean {

  private VariableRenderer renderer;
  private VariableRendererManager variableRendererManager;
  
  public void afterPropertiesSet() throws Exception {
    if(variableRendererManager != null && renderer != null) {
      variableRendererManager.addVariableRenderer(renderer);
    } else {
      throw new IllegalStateException("Both renderer and variableRenderManager should be set");
    }
  }
  
  public void setRenderer(VariableRenderer renderer) {
    this.renderer = renderer;
  }
  
  public void setVariableRendererManager(VariableRendererManager variableRendererManager) {
    this.variableRendererManager = variableRendererManager;
  }
}
