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

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;

import com.vaadin.ui.Field;


/**
 * Interface for rendering a {@link FormProperty} of a certain type in the Vaadin UI.
 * 
 * @author Frederik Heremans
 */
public interface FormPropertyRenderer {

  /**
   * The form type this renderer should be used for.
   */
  Class<? extends FormType> getFormType();
  
  /**
   * The component to show for the given form-property. Return null if
   * the form-property (and it's label) shouldn't be rendered. 
   */
  Field getPropertyField(FormProperty formProperty);
  
  /**
   * The label to use for the form-property.
   */
  String getPropertyLabel(FormProperty formProperty);
  
  /**
   * Extract the string representation of the value set in the field, for the 
   * given form property. This value is used as form property value when submitting
   * the (start)form to activiti. The field is the one created by 
   * {@link FormPropertyRenderer#getPropertyField(FormProperty)}.
   */
  String getFieldValue(FormProperty formProperty, Field field);
}
