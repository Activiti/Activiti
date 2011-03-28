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

package org.activiti.explorer.ui;

import org.activiti.engine.identity.User;
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.profile.ProfilePage;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class MainLayout extends CustomLayout {
  
  private static final long serialVersionUID = 4749017534074852514L;
  
  protected MainMenuBar mainMenuBar;
  
  public MainLayout() {
    super(ExplorerLayout.CUSTOM_LAYOUT_DEFAULT);
    setSizeFull();
    
    // Components visible on every page
    initSearchBox();
    initLogoutButton();
    initMainMenuBar();
  }
  
  protected void initSearchBox() {
    TextField searchBox = new TextField();
    searchBox.setInputPrompt(ExplorerApplication.getCurrent().getMessage(Messages.HEADER_SEARCHBOX));
    searchBox.addStyleName(ExplorerLayout.STYLE_SMALL_TEXTFIELD);
    searchBox.addStyleName(ExplorerLayout.STYLE_SEARCHBOX);
    addComponent(searchBox, ExplorerLayout.LOCATION_SEARCH);
  }
  
  @SuppressWarnings("serial")
  protected void initLogoutButton() {
    // Username + logout button is put into a small grid
    GridLayout logoutGrid = new GridLayout(2, 1);
    logoutGrid.setStyleName(ExplorerLayout.STYLE_LOGOUT_BUTTON);

    // User name + link to profile 
    final User user = ExplorerApplication.getCurrent().getLoggedInUser();
    Button userButton = new Button(user.getFirstName() + " " + user.getLastName());
    userButton.setIcon(Images.USER);
    userButton.addStyleName(ExplorerLayout.STYLE_USER_PROFILE);
    userButton.addStyleName(Reindeer.BUTTON_LINK);
    
    userButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        ExplorerApplication.getCurrent().switchView(new ProfilePage(user.getId()));
      }
    });

    // logout button
    Button logout = new Button(ExplorerApplication.getCurrent().getMessage(Messages.HEADER_LOGOUT));
    logout.setStyleName(Reindeer.BUTTON_LINK);
    logout.addStyleName(ExplorerLayout.STYLE_LOGOUT_BUTTON);
    logout.setIcon(Images.WHITE_DIVIDER);
    logout.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        ExplorerApplication.getCurrent().close();
      }
    });

    // Add components to grid layout
    logoutGrid.addComponent(userButton, 0, 0);
    logoutGrid.addComponent(logout, 1, 0);

    // Add logout grid to header
    addComponent(logoutGrid, ExplorerLayout.LOCATION_LOGOUT);
  }
  
  protected void initMainMenuBar() {
    this.mainMenuBar = new MainMenuBar();
    addComponent(mainMenuBar, ExplorerLayout.LOCATION_MAIN_MENU);
  }

}
