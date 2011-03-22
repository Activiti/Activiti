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

package org.activiti.explorer.ui.flow;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.ui.form.FormPropertiesComponent;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;


/**
 * Form for starting a process-definition using form-properties. Exposes
 * events to listen to OK and CANCEL events.
 * 
 * @author Frederik Heremans
 */
public class ProcessDefinitionStartForm extends VerticalLayout {
  
  private static final long serialVersionUID = -3197331726904715949L;

  // Members
  protected ProcessDefinition processDefinition;
  
  // Services
  protected FormService formService;

  // UI
  protected Button submitFormButton;
  protected Button cancelFormButton;
  protected FormPropertiesComponent formPropertiesComponent;
  
  public ProcessDefinitionStartForm(ProcessDefinition processDefinition) {
    super();
    this.processDefinition = processDefinition;
    
    formService = ProcessEngines.getDefaultProcessEngine().getFormService();
    
    initFormPropertiesComponent();
    initButtons();
    initListeners();
  }
  
  public void setFormProperties(List<FormProperty> formProperties) {
    // Component will render all properties
    formPropertiesComponent.setFormProperties(formProperties);
  }
  

  protected void initButtons() {
    submitFormButton = new Button("Start flow");
    cancelFormButton = new Button("Cancel");
    
    HorizontalLayout buttons = new HorizontalLayout();
    buttons.addComponent(submitFormButton);
    buttons.setComponentAlignment(submitFormButton, Alignment.BOTTOM_RIGHT);
    
    buttons.addComponent(cancelFormButton);
    buttons.setComponentAlignment(cancelFormButton, Alignment.BOTTOM_RIGHT);
    
    addComponent(buttons);
  }

  protected void initFormPropertiesComponent() {
    formPropertiesComponent = new FormPropertiesComponent();
    addComponent(formPropertiesComponent);    
    
    // Add whitespace
    addEmptySpace(this);
  }
  
  protected void initListeners() {
    submitFormButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = -6091586145870618870L;
      
      public void buttonClick(ClickEvent event) {
        Map<String, String> formProperties = formPropertiesComponent.getFormPropertyValues();
        for(Entry<String, String> entry : formProperties.entrySet()) {
          System.out.println(entry.getKey() + "->" + entry.getValue());
        }
        // Start process instance
        formService.submitStartFormData(processDefinition.getId(), formProperties);
        // Show notification of success
        ExplorerApplication.getCurrent().getMainWindow().showNotification("Process '" + processDefinition.getName() + "' has been started");
        fireEvent(new FormEvent(ProcessDefinitionStartForm.this, FormEvent.TYPE_SUBMIT));
      }
    });
    
    cancelFormButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = -8980500491522472381L;

      public void buttonClick(ClickEvent event) {
        fireEvent(new FormEvent(ProcessDefinitionStartForm.this, FormEvent.TYPE_CANCEL));
      }
    });
  }
  
  protected void addEmptySpace(ComponentContainer container) {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    container.addComponent(emptySpace);
  }
  
  // Event
  public class FormEvent extends Event {

    private static final long serialVersionUID = -410814526942034125L;
    
    public static final String TYPE_SUBMIT = "submit";
    public static final String TYPE_CANCEL = "cancel";
    
    private String type;
    
    public FormEvent(Component source, String type) {
      super(source);
      this.type = type;
    }
    
    public String getType() {
      return type;
    }
  }
}
