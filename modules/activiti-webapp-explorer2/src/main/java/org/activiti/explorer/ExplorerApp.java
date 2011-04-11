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

import org.activiti.engine.identity.User;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.util.LogUtil;
import org.activiti.explorer.cache.TrieBasedUserCache;
import org.activiti.explorer.cache.UserCache;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.MainWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;

/**
 * @author Joram Barrez
 */
@org.springframework.stereotype.Component
@Scope(value="session")
public class ExplorerApp extends Application implements HttpServletRequestListener {
  
  private static final long serialVersionUID = -1L;

  // Initialise logging
  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  // Thread local storage of instance for each user
  protected static ThreadLocal<ExplorerApp> current = new ThreadLocal<ExplorerApp>();
  
  // Application-wide services
  @Autowired
  protected TrieBasedUserCache userCache;
  
  // UI
  protected MainWindow mainWindow;
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  protected NotificationManager notificationManager;

  public void init() {
    initI18nManager();

    this.mainWindow = new MainWindow();
    initNotificationManager(); // Notifications depend on the mainWindow, so needs to be initialised afterwards
    initViewManager(); // Changing view depends on mainWindow, so needs to be initialised afterwards

    // UI
    setMainWindow(mainWindow);
    mainWindow.showLoginPage();
  }
  
  protected void initI18nManager() {
    this.i18nManager = new I18nManager(getLocale());
  }
  
  protected void initViewManager() {
    if (mainWindow == null) {
      throw new RuntimeException("Could not initialise ViewManager: null mainWindow");
    }
    this.viewManager = new ViewManager(mainWindow);
  }
  
  protected void initNotificationManager() {
    this.notificationManager = new NotificationManager(mainWindow, i18nManager);
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
  
  public static ExplorerApp get() {
    return current.get();
  }
  
  public User getLoggedInUser() {
    return (User) getUser();
  }
  
  // Managers
  
  public ViewManager getViewManager() {
    return viewManager;
  }
  
  public I18nManager getI18nManager() {
    return i18nManager;
  }
  
  public NotificationManager getNotificationManager() {
    return notificationManager;
  }
  
  // Application-wide services
  
  public UserCache getUserCache() {
    return userCache;
  }
  
  // HttpServletRequestListener -------------------------------------------------------------------
  
  public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
    // Set current application object as thread-local to make it easy accessible
    current.set(this);
    
   // Authentication: check if user is found, otherwise send to login page
    User user = (User) getUser();
    if (user == null) {
      if (mainWindow != null && !mainWindow.isShowingLoginPage()) {
        viewManager.showLoginPage();
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
  
  public UriFragment getCurrentUriFragment() {
    return mainWindow.getCurrentUriFragment();
  }
}
