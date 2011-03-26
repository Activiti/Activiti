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

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.User;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApplication;

import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.LoginForm.LoginListener;


/**
 * @author Joram Barrez
 */
public class LoginPage extends CustomLayout {
  
  private static final long serialVersionUID = 1L;
  
  protected IdentityService identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();

  public LoginPage() {
    super(Constants.LOGIN_LAYOUT);  // Layout is defined in /activiti/login.html + styles.css
    
    addStyleName(Constants.STYLE_LOGIN_PAGE);
    initUi();
  }
  
  protected void initUi() {
    // Login form is an atypical Vaadin component, since we want browsers to fill the password fields
    CustomLoginForm loginForm = new CustomLoginForm();
    addComponent(loginForm, Constants.LOCATION_LOGIN);
    
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
      
      boolean success = identityService.checkPassword(userName, password);
      if (success) {
        User user = identityService.createUserQuery().userId(userName).singleResult();
        ExplorerApplication.getCurrent().setUser(user);
        ExplorerApplication.getCurrent().showDefaultContent();
      } else {
        refreshUi();
        ExplorerApplication.getCurrent().showErrorNotification("Could not log you in", "Invalid user id and/or password");
      }
    }
  }

}
