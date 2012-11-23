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
package org.activiti.explorer.ui.management.processdefinition;

import java.util.Date;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.AbstractPage;
import org.activiti.explorer.ui.custom.PopupWindow;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Joram Barrez
 */
public class ChangeProcessSuspensionStatePopupWindow extends PopupWindow {

  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  protected String processDefinitionId;
  
  protected AbstractPage parentPage;
  protected VerticalLayout verticalLayout;
  protected CheckBox nowCheckBox;
  protected CheckBox dateCheckBox;
  protected DateField dateField;
  protected CheckBox includeProcessInstancesCheckBox;

  public ChangeProcessSuspensionStatePopupWindow(String processDefinitionId, AbstractPage parentPage, boolean suspend) {
    this.processDefinitionId = processDefinitionId;
    this.parentPage = parentPage;
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    setCaption(suspend ? i18nManager.getMessage(Messages.PROCESS_SUSPEND_POPUP) :
                         i18nManager.getMessage(Messages.PROCESS_ACTIVATE_POPUP));
    setModal(true);
    center();
    setResizable(false);
    setWidth(400, UNITS_PIXELS);
    setHeight(300, UNITS_PIXELS);
    addStyleName(Reindeer.WINDOW_LIGHT);
    
    verticalLayout = new VerticalLayout();
    addComponent(verticalLayout);
    addTimeSection(suspend);
    addIncludeProcessInstancesSection(suspend);
    addOkButton(suspend);
  }
  
  protected void addTimeSection(boolean suspend) {
    Label timeLabel = new Label(suspend ? i18nManager.getMessage(Messages.PROCESS_SUSPEND_POPUP_TIME_DESCRIPTION)
            : i18nManager.getMessage(Messages.PROCESS_ACTIVATE_POPUP_TIME_DESCRIPTION));
    verticalLayout.addComponent(timeLabel);
    verticalLayout.addComponent(new Label("&nbsp", Label.CONTENT_XHTML));
    
    nowCheckBox = new CheckBox(i18nManager.getMessage(Messages.PROCESS_SUSPEND_POPUP_TIME_NOW), true);
    nowCheckBox.addStyleName(ExplorerLayout.STYLE_PROCESS_DEFINITION_SUSPEND_CHOICE);
    nowCheckBox.setImmediate(true);
    nowCheckBox.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        if (nowCheckBox.booleanValue() == true) {
          dateField.setValue(null);
          dateCheckBox.setValue(false);
        } else {
          dateCheckBox.setValue(true);
          dateField.setValue(new Date());
        }
      }
    });
    verticalLayout.addComponent(nowCheckBox);
    
    HorizontalLayout dateLayout = new HorizontalLayout();
    verticalLayout.addComponent(dateLayout);
    
    dateCheckBox = new CheckBox(i18nManager.getMessage(Messages.PROCESS_SUSPEND_POPUP_TIME_DATE));
    dateCheckBox.addStyleName(ExplorerLayout.STYLE_PROCESS_DEFINITION_SUSPEND_CHOICE);
    dateCheckBox.setImmediate(true);
    dateCheckBox.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        if (dateCheckBox.booleanValue() == true) {
          dateField.setValue(new Date());
          nowCheckBox.setValue(false);
        } else {
          dateField.setValue(null);
          nowCheckBox.setValue(true);
        }
      }
    });
    dateLayout.addComponent(dateCheckBox);
    
    dateField = new DateField();
    dateField.setImmediate(true);
    dateField.addListener(new ValueChangeListener() {
      
      public void valueChange(ValueChangeEvent event) {
        if (dateField.getValue() != null) {
          nowCheckBox.setValue(false);
          dateCheckBox.setValue(true);
        }
      }
      
    });
    dateLayout.addComponent(dateField);
  }
  
  protected void addIncludeProcessInstancesSection(boolean suspend) {
    verticalLayout.addComponent(new Label("&nbsp", Label.CONTENT_XHTML));
    verticalLayout.addComponent(new Label("&nbsp", Label.CONTENT_XHTML));
    
    includeProcessInstancesCheckBox = new CheckBox(suspend ?
            i18nManager.getMessage(Messages.PROCESS_SUSPEND_POPUP_INCLUDE_PROCESS_INSTANCES_DESCRIPTION) :
            i18nManager.getMessage(Messages.PROCESS_ACTIVATE_POPUP_INCLUDE_PROCESS_INSTANCES_DESCRIPTION), true);
    verticalLayout.addComponent(includeProcessInstancesCheckBox);
  }
  
  protected void addOkButton(final boolean suspend) {
    verticalLayout.addComponent(new Label("&nbsp", Label.CONTENT_XHTML));
    verticalLayout.addComponent(new Label("&nbsp", Label.CONTENT_XHTML));
    
    Button okButton = new Button(i18nManager.getMessage(Messages.BUTTON_OK));
    verticalLayout.addComponent(okButton);
    verticalLayout.setComponentAlignment(okButton, Alignment.BOTTOM_CENTER);
    
    okButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
        boolean includeProcessInstances = (Boolean) includeProcessInstancesCheckBox.getValue();
                
        if (suspend) {
          repositoryService.suspendProcessDefinitionById(processDefinitionId, 
                includeProcessInstances, (Date) dateField.getValue());
        } else {
          repositoryService.activateProcessDefinitionById(processDefinitionId, 
                  includeProcessInstances, (Date) dateField.getValue());
        }
        
        close();
        parentPage.refreshSelectNext(); // select next item in list on the left
      }
      
    });
  }
  
}
