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

import java.util.List;
import java.util.Map;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * A component capable of rendering a form based on form-properties.
 * 
 * @author Frederik Heremans
 */
public class FormPropertiesComponent extends VerticalLayout {
  
  private static final long serialVersionUID = -6553042418486758961L;
  
  protected List<FormProperty> formProperties;
  protected Map<FormProperty, Component> propertyComponents;
  
  protected GridLayout propertyGrid;
  
  public FormPropertiesComponent() {
    
    super();
    propertyGrid = new GridLayout();
    propertyGrid.setColumns(2);
    propertyGrid.setSpacing(true);
    
    setSizeFull();
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    addComponent(propertyGrid);
  }
  
  public List<FormProperty> getFormProperties() {
    return formProperties;
  }
  
  public void setFormProperties(List<FormProperty> formProperties) {
    this.formProperties = formProperties;
    
    // Clear current components in the grid
    propertyGrid.removeAllComponents();
    
    if(formProperties != null) {
      for(FormProperty formProperty : formProperties) {
        FormPropertyRenderer renderer = null;
        
        FormType formPropertyType = formProperty.getType();
        if(formPropertyType == null) {
          renderer = FormPropertyMapping.getTypeLessFormPropertyRenderer();
        } else {
          renderer = FormPropertyMapping.getPropertyRendererForType(formProperty.getType());
        }
       
        Component editorComponent = renderer.getComponentProperty(formProperty);
        if(editorComponent != null) {
          // Get label for editor component.
          Component propertyLabel = renderer.getPropertyLabel(formProperty);
          propertyGrid.addComponent(propertyLabel);
          propertyGrid.addComponent(editorComponent);
        }
      }
    }
  }
  

  public Map<String, String> getFormPropertyValues() {
    // TODO: get values from properties
    return null;
  }
  
  
  
  
}
