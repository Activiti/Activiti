/*
 * Copyright 2009 IT Mill Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.activiti;

import com.vaadin.Application;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Joram Barrez
 */
public class ExplorerApplication extends Application {

  private static final long serialVersionUID = -8923370280251348552L;
  
  protected CustomLayout mainLayout;

  public void init() {
    
    final Window window = new Window("My pretty Vaadin Application");
    setMainWindow(window);
    setTheme(Constants.THEME);

    mainLayout = new CustomLayout(Constants.THEME);
    mainLayout.setSizeFull();
    window.setContent(mainLayout);

    initSearch();
    initLogout();
  }
  
  protected void initSearch() {
    TextField searchBox = new TextField();
    searchBox.setInputPrompt("Search tasks");
    searchBox.addStyleName("small");
    searchBox.addStyleName("searchBox");
    mainLayout.addComponent(searchBox, "search");
  }
  
  protected void initLogout() {
    GridLayout logoutGrid = new GridLayout(2, 1);
    logoutGrid.setStyleName("logout");

    // Add user 
    Button userButton = new Button("Kermit The Frog");
    userButton.setIcon(new ThemeResource("img/user-icon.png"));
    userButton.addStyleName("user");
    userButton.addStyleName(Reindeer.BUTTON_LINK);
    
    userButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        mainLayout.addComponent(new ProfilePanel(ExplorerApplication.this), "content");
      }
    });

    // Add logout button
    Button logout = new Button("Logout");
    logout.setStyleName(Reindeer.BUTTON_LINK);
    logout.addStyleName("logout");
    logout.setIcon(new ThemeResource("img/divider-white.png"));
    logout.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        ExplorerApplication.this.close();
      }
    });

    // Add components to grid
    logoutGrid.addComponent(userButton, 0, 0);
    logoutGrid.addComponent(logout, 1, 0);

    // Add logout grid to header
    mainLayout.addComponent(logoutGrid, "logout");
  }

}
