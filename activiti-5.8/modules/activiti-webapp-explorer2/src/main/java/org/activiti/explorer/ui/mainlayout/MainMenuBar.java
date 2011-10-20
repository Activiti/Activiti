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

package org.activiti.explorer.ui.mainlayout;

import java.util.HashMap;
import java.util.Map;

import org.activiti.explorer.Environments;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.identity.LoggedInUser;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.profile.ChangePasswordPopupWindow;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
@SuppressWarnings("serial")
public class MainMenuBar extends HorizontalLayout {

  private static final long serialVersionUID = 1L;
  
  protected ViewManager viewManager;
  protected I18nManager i18nManager;
  protected Map<String, Button> menuItemButtons;
  protected String currentMainNavigation;
  
  public MainMenuBar() {
    this.viewManager = ExplorerApp.get().getViewManager();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    menuItemButtons = new HashMap<String, Button>();
    init();
  }
  
  /**
   * Highlights the given main navigation in the menubar.
   */
  public synchronized void setMainNavigation(String navigation) {
    if(currentMainNavigation != null) {
      menuItemButtons.get(currentMainNavigation).removeStyleName(ExplorerLayout.STYLE_ACTIVE);
    }
    currentMainNavigation = navigation;
    
    Button current = menuItemButtons.get(navigation);
    if(current != null) {
      current.addStyleName(ExplorerLayout.STYLE_ACTIVE);
    }
  }
  
  protected void init() {
    setHeight(54, UNITS_PIXELS);
    setWidth(100, UNITS_PERCENTAGE);
    
    setMargin(false, true, false, false);
    
    initTitle();
    initButtons();
    initProfileButton();
  }
 
  protected void initButtons() {
    // TODO: fixed widths based on i18n strings?
    Button taskButton = addMenuButton(ViewManager.MAIN_NAVIGATION_TASK, i18nManager.getMessage(Messages.MAIN_MENU_TASKS), Images.MAIN_MENU_TASKS, false, 80);
    taskButton.addListener(new ShowTasksClickListener());
    menuItemButtons.put(ViewManager.MAIN_NAVIGATION_TASK, taskButton);
    
    Button processButton = addMenuButton(ViewManager.MAIN_NAVIGATION_PROCESS, i18nManager.getMessage(Messages.MAIN_MENU_PROCESS), Images.MAIN_MENU_PROCESS, false, 80);
    processButton.addListener(new ShowProcessDefinitionsClickListener());
    menuItemButtons.put(ViewManager.MAIN_NAVIGATION_PROCESS, processButton);

    if (ExplorerApp.get().getLoggedInUser().isAdmin()) {
      Button manageButton = addMenuButton(ViewManager.MAIN_NAVIGATION_MANAGE, i18nManager.getMessage(Messages.MAIN_MENU_MANAGEMENT), Images.MAIN_MENU_MANAGE, false, 90);
      manageButton.addListener(new ShowManagementClickListener());
      menuItemButtons.put(ViewManager.MAIN_NAVIGATION_MANAGE, manageButton);
    }
  }

  protected void initTitle() {
    Label title = new Label();
    title.addStyleName(Reindeer.LABEL_H1);
    
    if (ExplorerApp.get().getEnvironment().equals(Environments.ALFRESCO)) {
      title.addStyleName(ExplorerLayout.STYLE_WORKFLOW_CONSOLE_LOGO);
    } else {
      title.addStyleName(ExplorerLayout.STYLE_APPLICATION_LOGO);
    }
    
    addComponent(title);
    
    setExpandRatio(title, 1.0f);
  }

  protected Button addMenuButton(String type, String label, Resource icon, boolean active, float width) {
    Button button = new Button(label);
    button.addStyleName(type);
    button.addStyleName(ExplorerLayout.STYLE_MAIN_MENU_BUTTON);
    button.addStyleName(Reindeer.BUTTON_LINK);
    button.setHeight(54, UNITS_PIXELS);
    button.setIcon(icon);
    button.setWidth(width, UNITS_PIXELS);
    
    addComponent(button);
    setComponentAlignment(button, Alignment.TOP_CENTER);
    
    return button;
  }
  
  protected void initProfileButton() {
    final LoggedInUser user = ExplorerApp.get().getLoggedInUser();

    // User name + link to profile 
    MenuBar profileMenu = new MenuBar();
    profileMenu.addStyleName(ExplorerLayout.STYLE_HEADER_PROFILE_BOX);
    MenuItem rootItem = profileMenu.addItem(user.getFirstName() + " " + user.getLastName(), null);
    rootItem.setStyleName(ExplorerLayout.STYLE_HEADER_PROFILE_MENU);
    
    if(useProfile()) {
      // Show profile
      rootItem.addItem(i18nManager.getMessage(Messages.PROFILE_SHOW), new Command() {
        public void menuSelected(MenuItem selectedItem) {
          ExplorerApp.get().getViewManager().showProfilePopup(user.getId());
        }
      });
      
      // Edit profile
      rootItem.addItem(i18nManager.getMessage(Messages.PROFILE_EDIT), new Command() {
        
        public void menuSelected(MenuItem selectedItem) {
          // TODO: Show in edit-mode
          ExplorerApp.get().getViewManager().showProfilePopup(user.getId());
        }
      });
      
      // Change password
      rootItem.addItem(i18nManager.getMessage(Messages.PASSWORD_CHANGE), new Command() {
        public void menuSelected(MenuItem selectedItem) {
          ExplorerApp.get().getViewManager().showPopupWindow(new ChangePasswordPopupWindow());
        }
      });
      
      rootItem.addSeparator();
    }
   
    // Logout
    rootItem.addItem(i18nManager.getMessage(Messages.HEADER_LOGOUT), new Command() {
      public void menuSelected(MenuItem selectedItem) {
        ExplorerApp.get().close();
      }
    });

    addComponent(profileMenu);
    setComponentAlignment(profileMenu, Alignment.TOP_RIGHT);
    setExpandRatio(profileMenu, 1.0f);
  }
  
  protected boolean useProfile() {
    return true;
  }
  
  // Listener classes
  private class ShowTasksClickListener implements ClickListener {
    public void buttonClick(ClickEvent event) {
      ExplorerApp.get().getViewManager().showInboxPage();
    }
  }
  
  private class ShowProcessDefinitionsClickListener implements ClickListener {
    public void buttonClick(ClickEvent event) {
      ExplorerApp.get().getViewManager().showProcessDefinitionPage();
    }
  }
  
  private class ShowManagementClickListener implements ClickListener {
    public void buttonClick(ClickEvent event) {
      ExplorerApp.get().getViewManager().showDatabasePage();
    }
  }  
}
