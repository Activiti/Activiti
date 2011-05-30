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
package org.activiti.explorer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.util.LogUtil;
import org.activiti.explorer.cache.UserCache;
import org.activiti.explorer.identity.LoggedInUser;
import org.activiti.explorer.identity.LoggedInUserImpl;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.ComponentFactory;
import org.activiti.explorer.ui.MainWindow;
import org.activiti.explorer.ui.content.AttachmentRendererManager;
import org.activiti.explorer.ui.form.FormPropertyRendererManager;
import org.activiti.explorer.ui.login.LoginHandler;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;

/**
 * @author Joram Barrez
 */
public class ExplorerApp extends Application implements HttpServletRequestListener {
  
  private static final long serialVersionUID = -1L;

  // Initialise logging
  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  // Thread local storage of instance for each user
  protected static ThreadLocal<ExplorerApp> current = new ThreadLocal<ExplorerApp>();
  
  protected UserCache userCache;
  protected MainWindow mainWindow;
  protected ViewManager viewManager;
  protected NotificationManager notificationManager;
  protected I18nManager i18nManager;
  protected AttachmentRendererManager attachmentRendererManager;
  protected FormPropertyRendererManager formPropertyRendererManager;
  protected LoginHandler loginHandler;
  protected ComponentFactories componentFactories;
  
  public void init() {
    setMainWindow(mainWindow);
    mainWindow.showLoginPage();
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
  
  public LoggedInUser getLoggedInUser() {
    return (LoggedInUser) getUser();
  }
  
  // Managers (session scoped)
  
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
  
  public AttachmentRendererManager getAttachmentRendererManager() {
    return attachmentRendererManager;
  }
  
  public FormPropertyRendererManager getFormPropertyRendererManager() {
    return formPropertyRendererManager;
  }
  
  public void setFormPropertyRendererManager(FormPropertyRendererManager formPropertyRendererManager) {
    this.formPropertyRendererManager = formPropertyRendererManager;
  }

  public UserCache getUserCache() {
    return userCache;
  }
  
  public <T> ComponentFactory<T> getComponentFactory(Class<? extends ComponentFactory<T>> clazz) {
    return componentFactories.get(clazz);
  }
  
  public LoginHandler getLoginHandler() {
    return loginHandler;
  }
  
  // HttpServletRequestListener -------------------------------------------------------------------
  
  public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
    // Set current application object as thread-local to make it easy accessible
    current.set(this);
    
   // Authentication: check if user is found, otherwise send to login page
    LoggedInUser user = (LoggedInUser) getUser();
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
  
  // URL Handling ---------------------------------------------------------------------------------
  
  public void setCurrentUriFragment(UriFragment fragment) {
    mainWindow.setCurrentUriFragment(fragment);
  }
  public UriFragment getCurrentUriFragment() {
    return mainWindow.getCurrentUriFragment();
  }
  
  // Injection setters
  
  public void setUserCache(UserCache userCache) {
    this.userCache = userCache;
  }
  public void setApplicationMainWindow(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }
  public void setViewManager(ViewManager viewManager) {
    this.viewManager = viewManager;
  }
  public void setNotificationManager(NotificationManager notificationManager) {
    this.notificationManager = notificationManager;
  }
  public void setI18nManager(I18nManager i18nManager) {
    this.i18nManager = i18nManager;
  }
  public void setAttachmentRendererManager(AttachmentRendererManager attachmentRendererManager) {
    this.attachmentRendererManager = attachmentRendererManager;
  }
  public void setComponentFactories(ComponentFactories componentFactories) {
    this.componentFactories = componentFactories;
  }
  public void setLoginHandler(LoginHandler loginHandler) {
    this.loginHandler = loginHandler;
  }
}
