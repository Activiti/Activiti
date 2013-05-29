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
package org.activiti.editor.ui;

import org.activiti.engine.repository.Model;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.PopupWindow;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Joram Barrez
 */
public class DeployModelPopupWindow extends PopupWindow {

  private static final long serialVersionUID = 1L;

  protected GridLayout layout;
  protected Label descriptionLabel;
  protected TextField processNameTextField;
  protected CheckBox generateReportsCheckBox;
  protected Button deployButton;
  protected Button cancelButton;

  public DeployModelPopupWindow(Model modelData) {
    setWidth(400, UNITS_PIXELS);
    setModal(true);
    setResizable(false);

    addStyleName(Reindeer.PANEL_LIGHT);

    layout = new GridLayout(2, 2);
    layout.setSpacing(true);
    layout.setSizeFull();
    layout.setMargin(false, false, true, false);
    addComponent(layout);
    
    I18nManager i18nManager = ExplorerApp.get().getI18nManager();
    setCaption(i18nManager.getMessage(Messages.MODEL_DEPLOY_POPUP_CAPTION));
    
    // Process name
    Label nameLabel = new Label(i18nManager.getMessage(Messages.MODEL_DEPLOY_NAME));
    layout.addComponent(nameLabel, 0, 0);
    
    processNameTextField = new TextField();
    if (modelData.getName() != null) {
      processNameTextField.setValue(modelData.getName());
    }
    processNameTextField.focus();
    layout.addComponent(processNameTextField, 1, 0);
    
    // Generate reports
    Label generateReportsLabel = new Label(i18nManager.getMessage(Messages.MODEL_DEPLOY_GENERATE_REPORTS));
    layout.addComponent(generateReportsLabel, 0, 1);
    
    generateReportsCheckBox = new CheckBox();
    generateReportsCheckBox.setValue(true);
    layout.addComponent(generateReportsCheckBox, 1, 1);
    
    // Buttons
    initButtons(i18nManager);
  }

  /**
   * Show the confirmation popup.
   */
  public void showPopupWindow() {
    ExplorerApp.get().getViewManager().showPopupWindow(this);
  }

  protected void initButtons(I18nManager i18nManager) {
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setSpacing(true);
    buttonLayout.setWidth(100, UNITS_PERCENTAGE);
    addComponent(buttonLayout);
    
    deployButton = new Button(i18nManager.getMessage(Messages.MODEL_DEPLOY_BUTTON_DEPLOY));
    buttonLayout.addComponent(deployButton);
    buttonLayout.setComponentAlignment(deployButton, Alignment.BOTTOM_CENTER);
  }
  
  public void closePopupWindow() {
    close();
  }

  public Button getDeployButton() {
    return deployButton;
  }
  
  public void setDeployButton(Button deployButton) {
    this.deployButton = deployButton;
  }

  public Button getCancelButton() {
    return cancelButton;
  }

  public void setCancelButton(Button cancelButton) {
    this.cancelButton = cancelButton;
  }
  
  public String getProcessName() {
    return processNameTextField.getValue().toString();
  }
  
  public boolean isGenerateReports() {
    return generateReportsCheckBox.booleanValue();
  }
  
}
