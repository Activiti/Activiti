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

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.form.FormProperty;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.data.Validator.InvalidValueException;
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
 * Form that renders form-properties and allows posting the filled in value. Performs
 * validation as well. Exposes {@link FormPropertiesEvent}s which allow listening for 
 * submission and cancellation of the form.
 * 
 * @author Frederik Heremans
 */
public class FormPropertiesForm extends VerticalLayout {
  
  private static final long serialVersionUID = -3197331726904715949L;

  // Services
  protected transient FormService formService;
  protected I18nManager i18nManager;

  // UI
  protected Label formTitle;
  protected Button submitFormButton;
  protected Button cancelFormButton;
  protected FormPropertiesComponent formPropertiesComponent;
  
  public FormPropertiesForm() {
    super();
    formService = ProcessEngines.getDefaultProcessEngine().getFormService();
    i18nManager = ExplorerApp.get().getI18nManager();
    
    addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addStyleName(ExplorerLayout.STYLE_FORM_PROPERTIES);
    
    initTitle();
    initFormPropertiesComponent();
    initButtons();
    initListeners();
  }
  
  public void setFormProperties(List<FormProperty> formProperties) {
    // Component will refresh it's components based on the passed properties
    formPropertiesComponent.setFormProperties(formProperties);
  }
  
  public void setSubmitButtonCaption(String caption) {
    submitFormButton.setCaption(caption);
  }
  
  public void setCancelButtonCaption(String caption) {
    cancelFormButton.setCaption(caption);
  }
  
  public void setFormHelp(String caption) {
    formTitle.setValue(caption);
    formTitle.setVisible(caption != null);
  }
  
  /**
   * Clear all (writable) values in the form.
   */
  public void clear() {
    formPropertiesComponent.setFormProperties(formPropertiesComponent.getFormProperties());
  }

  protected void initTitle() {
    formTitle = new Label();
    formTitle.addStyleName(ExplorerLayout.STYLE_H4);
    formTitle.setVisible(false);
    addComponent(formTitle);
  }
 
  protected void initButtons() {
    submitFormButton = new Button();
    cancelFormButton = new Button();
    
    HorizontalLayout buttons = new HorizontalLayout();
    buttons.setSpacing(true);
    buttons.setWidth(100, UNITS_PERCENTAGE);
    buttons.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    buttons.addComponent(submitFormButton);
    buttons.setComponentAlignment(submitFormButton, Alignment.BOTTOM_RIGHT);
    
    buttons.addComponent(cancelFormButton);
    buttons.setComponentAlignment(cancelFormButton, Alignment.BOTTOM_RIGHT);
    
    Label buttonSpacer = new Label();
    buttons.addComponent(buttonSpacer);
    buttons.setExpandRatio(buttonSpacer, 1.0f);
    addComponent(buttons);
  }

  protected void initFormPropertiesComponent() {
    formPropertiesComponent = new FormPropertiesComponent();
    addComponent(formPropertiesComponent);    
  }
  
  protected void initListeners() {
    submitFormButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = -6091586145870618870L;
    
      public void buttonClick(ClickEvent event) {
        // Extract the submitted values from the form. Throws exception when validation fails.
        try {
          Map<String, String> formProperties = formPropertiesComponent.getFormPropertyValues();
          fireEvent(new FormPropertiesEvent(FormPropertiesForm.this, FormPropertiesEvent.TYPE_SUBMIT, formProperties));
          submitFormButton.setComponentError(null);
        } catch(InvalidValueException ive) {
          // Error is presented to user by the form component
        }
      }
    });
    
    cancelFormButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = -8980500491522472381L;

      public void buttonClick(ClickEvent event) {
        fireEvent(new FormPropertiesEvent(FormPropertiesForm.this, FormPropertiesEvent.TYPE_CANCEL));
        submitFormButton.setComponentError(null);
      }
    });
  }
  
  public void hideCancelButton() {
    cancelFormButton.setVisible(false);
  }
  
  protected void addEmptySpace(ComponentContainer container) {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    container.addComponent(emptySpace);
  }
  
  /**
   * Event indicating a form has been submitted or cancelled. When submitted,
   * the values of the form-properties are available.
   * 
   * @author Frederik Heremans
   */
  public class FormPropertiesEvent extends Event {

    private static final long serialVersionUID = -410814526942034125L;
    
    public static final String TYPE_SUBMIT = "SUBMIT";
    public static final String TYPE_CANCEL = "CANCEL";
    
    private String type;
    private Map<String, String> formProperties;
    
    public FormPropertiesEvent(Component source, String type) {
      super(source);
      this.type = type;
    }
    
    public FormPropertiesEvent(Component source, String type, Map<String, String> formProperties) {
      this(source, type);
      this.formProperties = formProperties;
    }
    
    public String getType() {
      return type;
    }
    
    public Map<String, String> getFormProperties() {
      return formProperties;
    }
  }
}
