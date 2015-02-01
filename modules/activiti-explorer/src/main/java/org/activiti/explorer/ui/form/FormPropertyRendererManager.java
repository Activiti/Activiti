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

package org.activiti.explorer.ui.form;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.form.FormType;


/**
 * @author Frederik Heremans
 * @author Joram Barrez
 * 
 * Note: NOT configured by @Component, but in Spring XML config, to make it
 * easy for users to extends with custom renderers.
 */
public class FormPropertyRendererManager implements Serializable {

  private static final long serialVersionUID = 1L;
  protected Map<Class<? extends FormType>, FormPropertyRenderer> propertyRenderers = new HashMap<Class<? extends FormType>, FormPropertyRenderer>();
  protected FormPropertyRenderer noTypePropertyRenderer;
  
  /**
   * Add a property-renderer for the given type. Overrides the existing
   * render (if present) for that type.
   */
  public void addFormPropertyRenderer(FormPropertyRenderer renderer) {
    propertyRenderers.put(renderer.getFormType(), renderer);
  }
  
  /**
   * Gets a {@link FormPropertyRenderer} for the given type.
   * 
   * @throws ActivitiException when no renderer is found for the given type.
   */
  public FormPropertyRenderer getPropertyRendererForType(FormType formType) {
    Class<? extends FormType> clazz = formType.getClass();
    FormPropertyRenderer renderer = propertyRenderers.get(clazz);
    
    if(renderer == null) {
      throw new ActivitiIllegalArgumentException("No property renderer found for type: " + 
        formType.getName() + ", " + formType.getClass());
    }
    return renderer;
  }
  
  public FormPropertyRenderer getTypeLessFormPropertyRenderer() {
    return noTypePropertyRenderer;
  }
  
  public void setNoTypePropertyRenderer(FormPropertyRenderer noTypePropertyRenderer) {
    this.noTypePropertyRenderer = noTypePropertyRenderer;
  }

  
  public void setPropertyRenderers(List<FormPropertyRenderer> propertyRenderers) {
    for (FormPropertyRenderer propertyRenderer : propertyRenderers) {
      addFormPropertyRenderer(propertyRenderer);
    }
  }
  
}
