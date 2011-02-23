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
package org.activiti.administrator.ui;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import org.activiti.administrator.AdminApp;
import org.activiti.administrator.ui.i18n.Messages;

/**
 * Login view
 * 
 * @author Patrick Oberg
 * 
 */
public class LoginView extends VerticalLayout {

  private static final long serialVersionUID = 1L;

  private final AdminApp app;

  private Button login = new Button("Login");
  private TextField username;
  private PasswordField password;

  @SuppressWarnings("serial")
  public LoginView(AdminApp application) {

    // Set application reference
    this.app = application;

    // Init window caption
    app.getMainWindow().setCaption(app.getMessage(Messages.Title));

    // Set style
    setStyleName(Reindeer.LAYOUT_WHITE);

    // Set layout to full size
    setSizeFull();

    // Create main layout
    VerticalLayout mainLayout = new VerticalLayout();

    // Add layout styles
    mainLayout.setStyleName(Reindeer.LAYOUT_WHITE);
    mainLayout.setWidth("100%");
    mainLayout.setHeight("100%");
    mainLayout.setMargin(false);
    mainLayout.setSpacing(false);

    // Add layout
    addComponent(mainLayout);
    setComponentAlignment(mainLayout, Alignment.TOP_LEFT);

    // Add field and button layout
    VerticalLayout buttonLayout = new VerticalLayout();
    buttonLayout.setSpacing(true);
    buttonLayout.setMargin(true);
    buttonLayout.setWidth("200px");
    buttonLayout.setStyleName("login-form");

    // Add username field
    username = new TextField(app.getMessage(Messages.Username));
    username.setWidth("100%");
    buttonLayout.addComponent(username);

    // Add password field
    password = new PasswordField(app.getMessage(Messages.Password));
    password.setWidth("100%");
    buttonLayout.addComponent(password);

    // Add Login button
    buttonLayout.addComponent(login);
    buttonLayout.setComponentAlignment(login, Alignment.BOTTOM_LEFT);

    // Add button layout
    mainLayout.addComponent(buttonLayout);

    // Add footer text
    Label footerText = new Label(app.getMessage(Messages.Footer));
    footerText.setSizeUndefined();
    footerText.setStyleName(Reindeer.LABEL_SMALL);
    footerText.addStyleName("footer");
    mainLayout.addComponent(footerText);
    mainLayout.setComponentAlignment(footerText, Alignment.BOTTOM_CENTER);

    // Set focus to this component
    username.focus();

    // Add shortcut to login button
    login.setClickShortcut(KeyCode.ENTER);

    login.addListener(new Button.ClickListener() {

      public void buttonClick(Button.ClickEvent event) {
        try {

          // Athenticate the user
          authenticate((String) username.getValue(), (String) password.getValue());

          // Switch to the main view
          app.switchView(MainView.class.getName(), new MainView(app));

        } catch (Exception e) {
          getWindow().showNotification(e.toString());
        }
      }
    });

  }

  /**
   * Checks the user credentials and authenticates the user if successful.
   * 
   * @param username
   * @param password
   * @throws Exception
   */
  public void authenticate(String username, String password) throws Exception {

    if (app.getAuthenticationService() == null) {
      throw new Exception(app.getMessage(Messages.ServiceUnavilable));
    } else if (!app.getAuthenticationService().authenticate(username, password)) {
      throw new Exception(app.getMessage(Messages.FailedLogin));
    } else {

      // Login successful, add user id to session
      app.setUser(username);

    }
  }
}