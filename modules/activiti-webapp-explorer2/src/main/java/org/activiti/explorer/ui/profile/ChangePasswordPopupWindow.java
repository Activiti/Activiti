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

package org.activiti.explorer.ui.profile;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class ChangePasswordPopupWindow extends Window {
  
  private static final long serialVersionUID = 1L;
  protected IdentityService identityService;
  protected String userId;
  protected I18nManager i18nManager;
  
  protected VerticalLayout layout;
  protected GridLayout inputGrid;
  protected PasswordField passwordField1;
  protected PasswordField passwordField2;
  
  public ChangePasswordPopupWindow() {
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.userId = ExplorerApp.get().getLoggedInUser().getId();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    setCaption(i18nManager.getMessage(Messages.PROFILE_CHANGE_PASSWORD));
    setModal(true);
    center();
    addStyleName(Reindeer.WINDOW_LIGHT);
    setWidth(350, UNITS_PIXELS);
    setHeight(205, UNITS_PIXELS);
    
    initLayout();
    initPasswordFields();
    initChangePasswordButton();
  }
  
  protected void initLayout() {
    layout = new VerticalLayout();
    layout.setMargin(true);
    layout.setSpacing(true);
    setContent(layout);
  }
  
  protected void initPasswordFields() {
    inputGrid = new GridLayout(2, 2);
    inputGrid.setSpacing(true);
    layout.addComponent(inputGrid);
    layout.setComponentAlignment(inputGrid, Alignment.MIDDLE_CENTER);
    
    Label newPasswordLabel = new Label(i18nManager.getMessage(Messages.PROFILE_NEW_PASSWORD));
    inputGrid.addComponent(newPasswordLabel);
    passwordField1 = new PasswordField();
    passwordField1.setWidth(150, UNITS_PIXELS);
    inputGrid.addComponent(passwordField1);

    Label confirmPasswordLabel = new Label(i18nManager.getMessage(Messages.PROFILE_CONFIRM_PASSWORD));
    inputGrid.addComponent(confirmPasswordLabel);
    passwordField2 = new PasswordField();
    passwordField2.setWidth(150, UNITS_PIXELS);
    inputGrid.addComponent(passwordField2);
  }
  
  protected void initChangePasswordButton() {
    Label emptySpace = new Label("&nbsp", Label.CONTENT_XHTML);
    emptySpace.setHeight(15, UNITS_PIXELS);
    layout.addComponent(emptySpace);
    
    Button changePasswordButton = new Button(i18nManager.getMessage(Messages.PROFILE_CHANGE_PASSWORD));
    layout.addComponent(changePasswordButton);
    layout.setComponentAlignment(changePasswordButton, Alignment.MIDDLE_CENTER);
    
    changePasswordButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        if (passwordField1.getValue() == null || "".equals(passwordField1.getValue().toString())) {
          
        }
      }
    });
  }

}
