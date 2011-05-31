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

import java.io.IOException;
import java.io.InputStream;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.identity.LoggedInUser;
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
  protected LoginHandler loginHandler;

  public LoginPage() {
    super();  
    
    // Check if the login HTML is available on the classpath. If present, the activiti-theme files are
    // inside a jar and should be loaded from here to be added as resource in UIDL, since the layout html 
    // is not present in a webapp-folder. If not found, just use the default way of defining the template, by name.
    InputStream loginHtmlStream = getClass().getResourceAsStream("/VAADIN/themes/" + ExplorerLayout.THEME + "/layouts/" 
            + ExplorerLayout.CUSTOM_LAYOUT_LOGIN + ".html");
    if(loginHtmlStream != null) {
      try {
        initTemplateContentsFromInputStream(loginHtmlStream);
      } catch (IOException e) {
        throw new RuntimeException("Error while loading login page template from classpath resource", e);
      }
    } else {
      setTemplateName(ExplorerLayout.CUSTOM_LAYOUT_LOGIN);
    }
    
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    this.notificationManager = ExplorerApp.get().getNotificationManager();
    this.loginHandler = ExplorerApp.get().getLoginHandler();
    
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
      // Delegate authentication to handler
      LoggedInUser loggedInUser = loginHandler.authenticate(userName, password);
      if (loggedInUser != null) {
        ExplorerApp.get().setUser(loggedInUser);
        viewManager.showDefaultPage();
      } else {
        refreshUi();
        notificationManager.showErrorNotification(Messages.LOGIN_FAILED_HEADER, i18nManager.getMessage(Messages.LOGIN_FAILED_INVALID));
      }
    }
  }

}
