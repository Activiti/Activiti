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

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.identity.User;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.util.LogUtil;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.MainWindow;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * @author Joram Barrez
 */
public class ExplorerApplication extends Application implements HttpServletRequestListener {
  
  private static final long serialVersionUID = -8923370280251348552L;
  
  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  protected static ThreadLocal<ExplorerApplication> current = new ThreadLocal<ExplorerApplication>();
  protected MainWindow mainWindow;
  protected ResourceBundle messages;

  public void init() {
    initResourceBundle();
    this.mainWindow = new MainWindow();
    setMainWindow(mainWindow);
    mainWindow.showLoginPage();
  }
  
  protected void initResourceBundle() {
    this.messages = ResourceBundle.getBundle(Constant.RESOURCE_BUNDLE, getLocale());
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
    mainWindow.switchView(component);
  }
  
  public void showLoginPage() {
    mainWindow.showLoginPage();
  }
  
  public void showDefaultContent() {
    mainWindow.showDefaultContent();
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
  
  // Localisation
  public String getMessage(String key) {
    return messages.getString(key);
  }

  public String getMessage(String key, Object... arguments) {
    return MessageFormat.format(messages.getString(key), arguments);
  }
  
  // HttpServletRequestListener -------------------------------------------------------------------
  
  public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
    // Set current application object as thread-local to make it easy accessible
    current.set(this);
    
   // Authentication: check if user is found, otherwise send to login page
    User user = (User) getUser();
    if (user == null) {
      if (mainWindow != null && !mainWindow.isShowingLoginPage()) {
        showLoginPage();
      }
    } else {
      // Set thread-local userid of logged in user (needed for Activiti user logic)
      Authentication.setAuthenticatedUserId(user.getId());
    }
  }
  
  public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
    // Clean up thread-locals
    current.remove();
    Authentication.setAuthenticatedUserId(null);
  }
  
  // URL handling ---------------------------------------------------------------------------------
  
  public void setCurrentUriFragment(UriFragment fragment) {
    mainWindow.setCurrentUriFragment(fragment);
  }
}
