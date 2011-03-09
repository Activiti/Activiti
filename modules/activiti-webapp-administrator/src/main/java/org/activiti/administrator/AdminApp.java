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
package org.activiti.administrator;

import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.vaadin.Application;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;
import org.activiti.administrator.service.AdminService;
import org.activiti.administrator.service.AuthenticationService;
import org.activiti.administrator.ui.Consts;
import org.activiti.administrator.ui.LoginView;
import org.activiti.administrator.ui.ViewManager;
import org.activiti.administrator.ui.i18n.Messages;

/**
 * Activiti Accelerator Administration Application
 * 
 * @author Patrick Oberg
 * 
 */
@Component(value = "adminApp")
@Scope(value = "session")
public class AdminApp extends Application implements ClickListener {

  private static final long serialVersionUID = 1L;

  @Autowired(required = false)
  AuthenticationService authenticationService;

  @Autowired(required = false)
  AdminService adminService;

  // Internationalized strings
  ResourceBundle i18nBundle;

  // View manager that handlers different screens in the UI
  ViewManager viewManager;

  // Application main window
  Window mainWindow;

  // General layout of the application
  protected CustomLayout mainLayout;

  // Buttons
  private Button logout = new Button("Logout", (ClickListener) this);

  @Override
  public void init() {

    // Set theme
    setTheme(Consts.THEME);

    // Set default locale
    setLocale(new Locale("en"));

    // Init resource bundle
    final ResourceBundle i18n = ResourceBundle.getBundle(Messages.class.getName(), getLocale());

    // Add title
    mainWindow = new Window(i18n.getString(Messages.AppTitle));

    // Set window to full size
    mainWindow.setSizeFull();

    // Set as main window
    setMainWindow(mainWindow);

    // Create main layout
    mainLayout = new CustomLayout(Consts.THEME);

    // Add styles
    mainLayout.addStyleName(Reindeer.LAYOUT_WHITE);
    mainLayout.setSizeFull();

    // Add layout to main window
    mainWindow.setContent(mainLayout);

    // Add window to view manager
    viewManager = new ViewManager(mainWindow);

    // Switch to the login screen
    switchView(LoginView.class.getName(), new LoginView(this));

  }

  /**
   * Switch view
   * 
   * @param name
   *          the name of the view class
   * @param view
   *          the view to switch to
   */
  public void switchView(String name, Layout view) {

    // Add view to main layout
    mainLayout.addComponent(view, Consts.CONTENT);

    // Add logout button if user is authenticated
    if ((getUser() != null) && !getUser().toString().isEmpty()) {

      // Create logout grid with user icon, user id and logout button
      GridLayout logoutGrid = new GridLayout(3, 1);
      logoutGrid.setStyleName("logout");

      // Add user icon
      Embedded userIcon = new Embedded(null, new ThemeResource("img/user-icon.png"));
      userIcon.setType(Embedded.TYPE_IMAGE);
      userIcon.addStyleName("icon");

      // Add user id
      Label userLabel = new Label(getUser().toString());
      userLabel.addStyleName("user");

      // Add logout button
      logout.setStyleName(Reindeer.BUTTON_LINK);
      logout.addStyleName("logout");
      logout.setIcon(new ThemeResource("img/divider-white.png"));

      // Add to logout grid
      logoutGrid.addComponent(userIcon, 0, 0);
      logoutGrid.addComponent(userLabel, 1, 0);
      logoutGrid.addComponent(logout, 2, 0);

      // Add logout grid to header
      mainLayout.addComponent(logoutGrid, Consts.LOGOUT);

    } else {

      // Remove logout button
      mainLayout.removeComponent(Consts.LOGOUT);

    }

    // Switch to new view
    viewManager.switchScreen(name, mainLayout);

  }

  /**
   * @return the authenticationService
   */
  public AuthenticationService getAuthenticationService() {
    return authenticationService;
  }

  /**
   * @return the adminService
   */
  public AdminService getAdminService() {
    return adminService;
  }

  /**
   * Returns the view manager used for controlling the different application
   * views.
   * 
   * @return viewManager the view manager
   */
  public ViewManager getViewManager() {
    // return view manager
    return viewManager;
  }

  @Override
  public void setLocale(Locale locale) {
    super.setLocale(locale);
    i18nBundle = ResourceBundle.getBundle(Messages.class.getName(), getLocale());
  }

  /** Returns the bundle for the current locale. */
  public ResourceBundle getBundle() {

    // Set to english by default
    if (i18nBundle == null) {
      setLocale(new Locale("en"));
    }

    // Return resource bundle
    return i18nBundle;
  }

  /**
   * Returns a localized message from the resource bundle with the current
   * application locale.
   **/
  public String getMessage(String key) {
    // return resource bundle
    return i18nBundle.getString(key);
  }

  public void buttonClick(ClickEvent event) {

    Button source = event.getButton();

    if (source == logout) {

      // Close application
      getMainWindow().getApplication().close();

      // Remove the user
      setUser(null);

    }
  }

}
