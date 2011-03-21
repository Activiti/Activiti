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
import org.activiti.explorer.Constants;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;


/**
 * @author Frederik Heremans
 */
public abstract class AbstractFormPropertyRenderer implements FormPropertyRenderer {

  private Class<? extends FormType> formType;
  
  public AbstractFormPropertyRenderer(Class< ? extends FormType> formType) {
    this.formType = formType;
  }

  public Class< ? extends FormType> getFormType() {
    return formType;
  }

  /**
   * Returns a simple {@link Label} with the name of the form-property. Reverts
   * to the property id if name is null.
   */
  public Component getPropertyLabel(FormProperty formProperty) {
    Label propertyLabel = new Label();
    propertyLabel.addStyleName(Constants.STYLE_FORMPROPERTY_LABEL);
    if(formProperty.getName() != null) {
      propertyLabel.setValue(formProperty.getName());
    } else {
      propertyLabel.setValue(formProperty.getId());
    }
    return propertyLabel;
  }
  
  public abstract Component getComponentProperty(FormProperty formProperty);
  
  /**
   * Gets the default component to render a read-only property value. This is
   * a simple label with {@link Constants#STYLE_FORMPROPERTY_READONLY} applied.
   */
  protected Component getDefaultReadonlyPropertyComponent(FormProperty formProperty) {
    Label readonlyValue = new Label();
    readonlyValue.addStyleName(Constants.STYLE_FORMPROPERTY_READONLY);
    if(formProperty.getValue() != null) {
      readonlyValue.setValue(formProperty.getValue());
    }
    return readonlyValue;
  }
}
