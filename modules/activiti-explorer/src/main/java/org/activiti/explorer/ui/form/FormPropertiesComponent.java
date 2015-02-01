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
import java.util.List;
import java.util.Map;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;
import org.activiti.explorer.ExplorerApp;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.VerticalLayout;


/**
 * A component capable of rendering a form based on form-properties and
 * extracting values filled in into the writable fields.
 * 
 * @author Frederik Heremans
 */
public class FormPropertiesComponent extends VerticalLayout {
  
  private static final long serialVersionUID = 1L;
  
  protected FormPropertyRendererManager formPropertyRendererManager;
  protected List<FormProperty> formProperties;
  protected Map<FormProperty, Component> propertyComponents;
  
  protected Form form;
  
  public FormPropertiesComponent() {
    this.formPropertyRendererManager = ExplorerApp.get().getFormPropertyRendererManager();
    
    setSizeFull();
    initForm();
  } 

  public List<FormProperty> getFormProperties() {
    return formProperties;
  }
  
  public void setFormProperties(List<FormProperty> formProperties) {
    this.formProperties = formProperties;
    
    form.removeAllProperties();
    
    // Clear current components in the grid
    if(formProperties != null) {
      for(FormProperty formProperty : formProperties) {
        FormPropertyRenderer renderer = getRenderer(formProperty);
       
        // Be able to get the Form from any Renderer.
        renderer.setForm(form);
       
        Field editorComponent = renderer.getPropertyField(formProperty);
        if(editorComponent != null) {
          // Get label for editor component.
          form.addField(formProperty.getId(), editorComponent);
        }
      }
    }
  }
  

  /**
   * Returns all values filled in in the writable fields on the form.
   * 
   * @throws InvalidValueException when a validation error occurs.
   */
  public Map<String, String> getFormPropertyValues() throws InvalidValueException {
    // Commit the form to ensure validation is executed
    form.commit();
    
    Map<String, String> formPropertyValues = new HashMap<String, String>();
    
    // Get values from fields defined for each form property
    for(FormProperty formProperty : formProperties) {
      if(formProperty.isWritable()) {
        Field field = form.getField(formProperty.getId());
        FormPropertyRenderer renderer = getRenderer(formProperty);
        String fieldValue = renderer.getFieldValue(formProperty, field);
        
        formPropertyValues.put(formProperty.getId(), fieldValue);
      }
    }
    return formPropertyValues;
  }
  
  
  public void setFormEnabled(boolean enabled) {
    if(enabled) {
      form.setEnabled(enabled);
    }  
  }
  
  protected void initForm() {
    form = new Form();
    form.setSizeFull();
    
    addComponent(form);
    setComponentAlignment(form, Alignment.TOP_CENTER);
  }

  protected FormPropertyRenderer getRenderer(FormProperty formProperty) {
    FormType formPropertyType = formProperty.getType();
    if(formPropertyType == null) {
      return formPropertyRendererManager.getTypeLessFormPropertyRenderer();
    } else {
      return formPropertyRendererManager.getPropertyRendererForType(formProperty.getType());
    }
  }
}
