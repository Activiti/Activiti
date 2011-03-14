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

import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApplication;

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
  
  protected ViewManager viewManager;
  protected MainMenuBar mainMenuBar;
  
  public MainLayout(ExplorerApplication explorerApplication) {
    super(Constants.THEME);
    setSizeFull();
    
    this.viewManager = new ViewManager(explorerApplication, this);
    
    // Components visible on every page
    initSearchBox();
    initLogoutButton();
    initMainMenuBar();
  }
  
  protected void initSearchBox() {
    TextField searchBox = new TextField();
    searchBox.setInputPrompt("Search tasks");
    searchBox.addStyleName(Constants.STYLE_SMALL_TEXTFIELD);
    searchBox.addStyleName(Constants.STYLE_SEARCHBOX);
    addComponent(searchBox, Constants.LOCATION_SEARCH);
  }
  
  @SuppressWarnings("serial")
  protected void initLogoutButton() {
    // Username + logout button is put into a small grid
    GridLayout logoutGrid = new GridLayout(2, 1);
    logoutGrid.setStyleName(Constants.STYLE_LOGOUT_BUTTON);

    // User name + link to profile 
    Button userButton = new Button("Kermit The Frog");
    userButton.setIcon(viewManager.getThemeResource(Constants.IMAGE_USER));
    userButton.addStyleName(Constants.STYLE_USER_LABEL);
    userButton.addStyleName(Reindeer.BUTTON_LINK);
    
    userButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        viewManager.switchView(Constants.VIEW_PROFILE, new ProfilePage(viewManager));
      }
    });

    // logout button
    Button logout = new Button("Logout");
    logout.setStyleName(Reindeer.BUTTON_LINK);
    logout.addStyleName(Constants.STYLE_LOGOUT_BUTTON);
    logout.setIcon(viewManager.getThemeResource(Constants.IMAGE_DIVIDER));
    logout.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        viewManager.getApplication().close();
      }
    });

    // Add components to grid layout
    logoutGrid.addComponent(userButton, 0, 0);
    logoutGrid.addComponent(logout, 1, 0);

    // Add logout grid to header
    addComponent(logoutGrid, "logout");
  }
  
  protected void initMainMenuBar() {
    this.mainMenuBar = new MainMenuBar(viewManager);
    addComponent(mainMenuBar, Constants.LOCATION_MAIN_MENU);
  }

}
