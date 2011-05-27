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
package org.activiti.explorer.ui.login;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.LoggedInUser;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.LoginForm.LoginListener;


/**
 * @author Joram Barrez
 */
public class LoginPage extends CustomLayout {
  
  private static final long serialVersionUID = 1L;
  
  protected IdentityService identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
  
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  protected NotificationManager notificationManager;

  public LoginPage() {
    super(ExplorerLayout.CUSTOM_LAYOUT_LOGIN);  // Layout is defined in /activiti/login.html + styles.css
    
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    this.notificationManager = ExplorerApp.get().getNotificationManager();
    
    addStyleName(ExplorerLayout.STYLE_LOGIN_PAGE);
    initUi();
  }
  
  protected void initUi() {
    // Login form is an a-typical Vaadin component, since we want browsers to fill the password fields
    // which is not the case for ajax-generated UI components
    ExplorerLoginForm loginForm = new ExplorerLoginForm();
    addComponent(loginForm, ExplorerLayout.LOCATION_LOGIN);
    
    // Login listener
    loginForm.addListener(new ActivitiLoginListener());
  }
  
  protected void refreshUi() {
    // Quick and dirty 'refresh'
    removeAllComponents();
    initUi();
  }
  
  class ActivitiLoginListener implements LoginListener {
    
    private static final long serialVersionUID = 1L;
    
    public void onLogin(LoginEvent event) {
      String userName = event.getLoginParameter("username"); // see the input field names in CustomLoginForm
      String password = event.getLoginParameter("password");  // see the input field names in CustomLoginForm
      
      if (identityService.checkPassword(userName, password)) {
        User user = identityService.createUserQuery().userId(userName).singleResult();
        
        // Fetch and cache user data
        LoggedInUser loggedInUser = new LoggedInUser(user, password);
        List<Group> groups = identityService.createGroupQuery().groupMember(user.getId()).list();
        for (Group group : groups) {
          if (Constants.SECURITY_ROLE.equals(group.getType())) {
            loggedInUser.addSecurityRoleGroup(group);
            if (Constants.SECURITY_ROLE_USER.equals(group.getId())) {
              loggedInUser.setUser(true);
            }
            if (Constants.SECURITY_ROLE_ADMIN.equals(group.getId())) {
              loggedInUser.setAdmin(true);
            }
          } else {
            loggedInUser.addGroup(group);
          }
        }
        
        ExplorerApp.get().setUser(loggedInUser);
        viewManager.showDefaultPage();
      } else {
        refreshUi();
        notificationManager.showErrorNotification(Messages.LOGIN_FAILED_HEADER, i18nManager.getMessage(Messages.LOGIN_FAILED_INVALID));
      }
    }
  }

}
