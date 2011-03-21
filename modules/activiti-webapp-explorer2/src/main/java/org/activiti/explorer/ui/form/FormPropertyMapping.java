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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.form.FormType;


/**
 * @author Frederik Heremans
 */
public class FormPropertyMapping {

  private static Map<Class<? extends FormType>, FormPropertyRenderer> propertyRenderers =  new HashMap<Class<? extends FormType>, FormPropertyRenderer>();
  private static FormPropertyRenderer noTypePropertyRenderer;
  
  /**
   * Add a property-renderer for the given type. Overrides the existing
   * render (if present) for that type.
   */
  public static void addFormPropertyRenderer(FormPropertyRenderer renderer) {
    propertyRenderers.put(renderer.getFormType(), renderer);
  }
  
  /**
   * Gets a {@link FormPropertyRenderer} for the given type.
   * 
   * @throws ActivitiException when no renderer is found for the given type.
   */
  public static FormPropertyRenderer getPropertyRendererForType(FormType formType) {
    Class<? extends FormType> clazz = formType.getClass();
    FormPropertyRenderer renderer = propertyRenderers.get(clazz);
    
    if(renderer == null) {
      throw new ActivitiException("No property renderer found for type: " + 
        formType.getName() + ", " + formType.getClass());
    }
    return renderer;
  }
  
  public static FormPropertyRenderer getTypeLessFormPropertyRenderer() {
    return noTypePropertyRenderer;
  }
  
  public static void setNoTypePropertyRenderer(FormPropertyRenderer noTypePropertyRenderer) {
    FormPropertyMapping.noTypePropertyRenderer = noTypePropertyRenderer;
  }
}
