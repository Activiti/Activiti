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
package org.activiti.explorer.ui.reports;

import java.util.Map;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.PopupWindow;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class SaveReportPopupWindow extends PopupWindow {
  
  private static final long serialVersionUID = 1L;
  
  protected String processDefinitionId;
  protected Map<String, String> originalFormProperties;
  protected Component componentToDisableOnClose;
  
  protected TextField nameField;

  public SaveReportPopupWindow() {
    I18nManager i18nManager = ExplorerApp.get().getI18nManager();
    setCaption(i18nManager.getMessage(Messages.REPORTING_SAVE_POPUP_CAPTION));
    
    VerticalLayout layout = new VerticalLayout();
    addComponent(layout);
    
    createNameTextField(i18nManager, layout);
    createSaveButton(i18nManager, layout);
    
    setModal(true);
    center();
    setResizable(false);
    setWidth(400, UNITS_PIXELS);
    setHeight(150, UNITS_PIXELS);
    addStyleName(Reindeer.WINDOW_LIGHT);
  }

  protected void createNameTextField(I18nManager i18nManager, VerticalLayout layout) {
    HorizontalLayout fieldLayout = new HorizontalLayout();
    fieldLayout.setWidth(100, UNITS_PERCENTAGE);
    layout.addComponent(fieldLayout);
    fieldLayout.addComponent(new Label(i18nManager.getMessage(Messages.REPORTING_SAVE_POPUP_NAME)));
    nameField = new TextField();
    nameField.setWidth(250, UNITS_PIXELS);
    nameField.focus();
    fieldLayout.addComponent(nameField);
  }

  protected void createSaveButton(final I18nManager i18nManager, final VerticalLayout layout) {
    layout.addComponent(new Label("&nbsp", Label.CONTENT_XHTML));
    Button saveButton = new Button(i18nManager.getMessage(Messages.BUTTON_SAVE));
    layout.addComponent(saveButton);
    layout.setComponentAlignment(saveButton, Alignment.MIDDLE_CENTER);
    
    saveButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        
        String reportName = null; 

        // Validate
        String error = null;
        if (nameField.getValue() == null || ((String) nameField.getValue()).length() == 0) {
          error = i18nManager.getMessage(Messages.REPORTING_SAVE_POPUP_NAME_EMPTY);
        } else {
          reportName = ExplorerApp.get().getLoggedInUser().getId() + "_" + nameField.getValue();
          if (reportName.length() > 255) {
            error = i18nManager.getMessage(Messages.REPORTING_SAVE_POPUP_NAME_TOO_LONG);
          } else {
            boolean nameUsed = ProcessEngines.getDefaultProcessEngine().getHistoryService()
                    .createHistoricProcessInstanceQuery().processInstanceBusinessKey(reportName).count() != 0;
            if (nameUsed) {
              error = i18nManager.getMessage(Messages.REPORTING_SAVE_POPUP_NAME_EXISTS);
            }
          }
        }
        
        if (error != null) {
          
          setHeight(185, UNITS_PIXELS);
          layout.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
          
          Label errorLabel = new Label(error);
          errorLabel.addStyleName(ExplorerLayout.STYLE_ERROR);
          layout.addComponent(errorLabel);
          
        } else {
        
          // Re-run reports to store the data for good now (the previous process instance was deleted)
          if (originalFormProperties != null) {
            startProcessInstanceWithFormProperties(reportName);
          } else {
            startProcessInstance(reportName);
          }
          
          // Remove the popup
          if (componentToDisableOnClose != null) {
            componentToDisableOnClose.setEnabled(false);
          }
          close();
          
        }
      }
      
    });
  }
  
  protected ProcessInstance startProcessInstanceWithFormProperties(String businessKey) {
    return ProcessEngines.getDefaultProcessEngine().getFormService()
            .submitStartFormData(processDefinitionId, businessKey, originalFormProperties);
  }
  
  protected ProcessInstance startProcessInstance(String businessKey) {
    return ProcessEngines.getDefaultProcessEngine().getRuntimeService().startProcessInstanceById(processDefinitionId, businessKey);
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  
  public Map<String, String> getOriginalFormProperties() {
    return originalFormProperties;
  }

  public void setOriginalFormProperties(Map<String, String> originalFormProperties) {
    this.originalFormProperties = originalFormProperties;
  }
  
  public Component getComponentToDisableOnClose() {
    return componentToDisableOnClose;
  }

  public void setComponentToDisableOnClose(Component componentToDisableOnClose) {
    this.componentToDisableOnClose = componentToDisableOnClose;
  }
  
}
