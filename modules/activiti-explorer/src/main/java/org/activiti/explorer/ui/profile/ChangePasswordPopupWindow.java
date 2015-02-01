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
import org.activiti.engine.identity.User;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.identity.LoggedInUser;
import org.activiti.explorer.ui.custom.PopupWindow;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class ChangePasswordPopupWindow extends PopupWindow {
  
  private static final long serialVersionUID = 1L;
  
  protected transient IdentityService identityService;
  protected LoggedInUser currentUser;
  protected I18nManager i18nManager;
  
  protected VerticalLayout layout;
  protected GridLayout inputGrid;
  protected PasswordField passwordField1;
  protected PasswordField passwordField2;
  protected Label errorLabel;
  
  public ChangePasswordPopupWindow() {
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.currentUser = ExplorerApp.get().getLoggedInUser();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    setCaption(i18nManager.getMessage(Messages.PASSWORD_CHANGE));
    setModal(true);
    center();
    addStyleName(Reindeer.WINDOW_LIGHT);
    setWidth(350, UNITS_PIXELS);
    setHeight(205, UNITS_PIXELS);
    
    initLayout();
    initPasswordFields();
    initChangePasswordButton();
    initEnterKeyListener();
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
    passwordField1.focus();

    Label confirmPasswordLabel = new Label(i18nManager.getMessage(Messages.PROFILE_CONFIRM_PASSWORD));
    inputGrid.addComponent(confirmPasswordLabel);
    passwordField2 = new PasswordField();
    passwordField2.setWidth(150, UNITS_PIXELS);
    inputGrid.addComponent(passwordField2);
  }
  
  protected void initChangePasswordButton() {
    errorLabel = new Label("&nbsp", Label.CONTENT_XHTML);
    errorLabel.addStyleName(Reindeer.LABEL_SMALL);
    errorLabel.addStyleName(ExplorerLayout.STYLE_LABEL_RED);
    layout.addComponent(errorLabel);
    
    Button changePasswordButton = new Button(i18nManager.getMessage(Messages.PASSWORD_CHANGE));
    layout.addComponent(changePasswordButton);
    layout.setComponentAlignment(changePasswordButton, Alignment.MIDDLE_CENTER);
    
    changePasswordButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        handlePasswordChange();
      }
    });
  }
  
  protected void initEnterKeyListener() {
    addActionHandler(new Handler() {
      public void handleAction(Action action, Object sender, Object target) {
        handlePasswordChange();
      }
      public Action[] getActions(Object target, Object sender) {
        return new Action[] {new ShortcutAction("enter", ShortcutAction.KeyCode.ENTER, null)};
      }
    });
  }
  
  protected void handlePasswordChange() {
    if (passwordField1.getValue() == null || "".equals(passwordField1.getValue().toString())
            || passwordField2.getValue() == null || "".equals(passwordField2.getValue().toString())) {
      errorLabel.setValue(i18nManager.getMessage(Messages.PASSWORD_CHANGE_INPUT_REQUIRED));
    } else if (!passwordField1.getValue().equals(passwordField2.getValue())){
      errorLabel.setValue(i18nManager.getMessage(Messages.PASSWORD_CHANGE_INPUT_MATCH));
    } else {
      String password = passwordField1.getValue().toString();
      // Change data
      User user = identityService.createUserQuery().userId(currentUser.getId()).singleResult();
      user.setPassword(password);
      identityService.saveUser(user);
      
      // Refresh logged in user session data
      ExplorerApp.get().setUser(ExplorerApp.get().getLoginHandler().authenticate(
          user.getId(), user.getPassword()));
      
      // Close popup
      close();
      
      // Show notification
      ExplorerApp.get().getNotificationManager().showInformationNotification(Messages.PASSWORD_CHANGED_NOTIFICATION);
    }
  }

}
