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
package org.activiti.explorer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.explorer.navigation.NavigationFragmentChangeListener;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.MainLayout;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * @author Joram Barrez
 */
public class ExplorerApplication extends Application implements HttpServletRequestListener {

  private static final long serialVersionUID = -8923370280251348552L;
  
  private static ThreadLocal<ExplorerApplication> current = new ThreadLocal<ExplorerApplication>();
  
  protected Window mainWindow;
  protected MainLayout mainLayout;
  protected UriFragmentUtility uriFragmentUtility;
  protected UriFragment currentUriFragment;

  public void init() {
    
    // Demo
    setUser(ProcessEngines.getDefaultProcessEngine().getIdentityService()
             .createUserQuery().userId("kermit").singleResult());
    ProcessEngines.getDefaultProcessEngine().getIdentityService().setAuthenticatedUserId("kermit");
    // Demo
    
    // init window
    mainWindow = new Window("Explorer - The Next generation");
    setMainWindow(mainWindow);
    setTheme(Constants.THEME);
    
    // init general look and feel
    mainLayout = new MainLayout(this); 
    mainWindow.setContent(mainLayout);
    
    // init hidden components
    initHiddenComponents();
  }
  
  /**
   *  Required to support multiple browser windows/tabs, 
   *  see http://vaadin.com/web/joonas/wiki/-/wiki/Main/Supporting%20Multible%20Tabs
   */
//  public Window getWindow(String name) {
//    Window window = super.getWindow(name);
//    if (window == null) {
//      window = new Window("Explorer - The Next generation");
//      window.setName(name);
//      addWindow(window);
//      window.open(new ExternalResource(window.getURL()));
//    }
//
//    return window;
//  }
  
  // View management ------------------------------------------------------------------------------
  
  public static ExplorerApplication getCurrent() {
    return current.get();
  }
  
  public void switchView(Component component) {
    mainLayout.addComponent(component, Constants.LOCATION_CONTENT);
  }
  
  public User getLoggedInUser() {
    return (User) getUser();
  }
  
  public void showErrorNotification(String caption, String message) {
    getMainWindow().showNotification(caption, "<br/>"+message, Notification.TYPE_ERROR_MESSAGE);
  }
  
  public void showInformationNotification(String message) {
    getMainWindow().showNotification(message, Notification.TYPE_HUMANIZED_MESSAGE);
  }
  
  public void showPopupWindow(Window window) {
    getMainWindow().addWindow(window);
  }
  
  // HttpServletRequestListener -------------------------------------------------------------------
  
  public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
    // Set current application object as thread-local to make it easy accessible
    current.set(this);
    
    // Set thread-local userid of logged in user (needed for Activiti user logic)
    User user = (User) getUser();
    if (user != null) {
      Authentication.setAuthenticatedUserId("kermit");
    }
  }
  
  public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
    // Clean up thread-locals
    current.remove();
    Authentication.setAuthenticatedUserId(null);
  }
  
  // URL handling ---------------------------------------------------------------------------------
  
  protected void initHiddenComponents() {
    // Add the URI Fragent utility
    uriFragmentUtility = new UriFragmentUtility();
    mainLayout.addComponent(uriFragmentUtility, Constants.LOCATION_HIDDEN);
    
    // Add listener to control page flow based on URI
    uriFragmentUtility.addListener(new NavigationFragmentChangeListener());
  }
  
  public UriFragment getCurrentUriFragment() {
    return currentUriFragment;
  }

  /**
   * Sets the current {@link UriFragment}. 
   * Won't trigger navigation, just updates the URI fragment in the browser.
   */
  public void setCurrentUriFragment(UriFragment fragment) {
    this.currentUriFragment = fragment;
    
    if(fragmentChanged(fragment)) {
      
      if(fragment != null) {
        uriFragmentUtility.setFragment(fragment.toString(), false);      
      } else {
        uriFragmentUtility.setFragment("", false);      
      }
    }
  }

  private boolean fragmentChanged(UriFragment fragment) {
    String fragmentString = fragment.toString();
    if(fragmentString == null) {
      return uriFragmentUtility.getFragment() != null;
    } else {
      return !fragmentString.equals(uriFragmentUtility.getFragment());
    }
  }
}
