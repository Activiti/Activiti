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

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.crystalball.simulator.SimulationDebugger;
import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.explorer.identity.LoggedInUser;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.ComponentFactory;
import org.activiti.explorer.ui.MainWindow;
import org.activiti.explorer.ui.content.AttachmentRendererManager;
import org.activiti.explorer.ui.form.FormPropertyRendererManager;
import org.activiti.explorer.ui.login.LoginHandler;
import org.activiti.explorer.ui.variable.VariableRendererManager;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.converter.json.SimpleWorkflowJsonConverter;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;

/**
 * @author Joram Barrez
 */
public class ExplorerApp extends Application implements HttpServletRequestListener {
  
  private static final long serialVersionUID = -1L;

  // Thread local storage of instance for each user
  protected transient static ThreadLocal<ExplorerApp> current = new ThreadLocal<ExplorerApp>();
  
  protected String environment;
  protected boolean useJavascriptDiagram;
  protected MainWindow mainWindow;
  protected ViewManager viewManager;
  protected NotificationManager notificationManager;
  protected I18nManager i18nManager;
  protected AttachmentRendererManager attachmentRendererManager;
  protected FormPropertyRendererManager formPropertyRendererManager;
  protected VariableRendererManager variableRendererManager;
  protected LoginHandler loginHandler;
  protected ComponentFactories componentFactories;
  protected WorkflowDefinitionConversionFactory workflowDefinitionConversionFactory;
  protected SimpleWorkflowJsonConverter simpleWorkflowJsonConverter;
  
  // Flag to see if the session has been invalidated, when the application was closed
  protected boolean invalidatedSession = false;
  
  protected List<String> adminGroups;
  protected List<String> userGroups;
  
  protected String crystalBallCurrentDefinitionId = null;
  protected String crystalBallCurrentInstanceId = null;
  protected List<SimulationEvent> crystalBallSimulationEvents = null;
  protected transient SimulationDebugger crystalBallSimulationDebugger = null;
  
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
//      window = new Window("Activiti Explorer");
//      window.setName(name);
//      addWindow(window);
//      window.open(new ExternalResource(window.getURL()));
//    }
//
//    return window;
//  }
  
  @Override
  public void close() {
    final LoggedInUser theUser = getLoggedInUser();
    
    // Clear the logged in user
    setUser(null);
    
    // Call loginhandler
    getLoginHandler().logout(theUser);
    
    invalidatedSession = false;
    super.close();
  }
  
  public static ExplorerApp get() {
    return current.get();
  }
  
  public LoggedInUser getLoggedInUser() {
    return (LoggedInUser) getUser();
  }
  
  public String getEnvironment() {
    return environment;
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

  public <T> ComponentFactory<T> getComponentFactory(Class<? extends ComponentFactory<T>> clazz) {
    return componentFactories.get(clazz);
  }
  
  public LoginHandler getLoginHandler() {
    return loginHandler;
  }
  
  public void setVariableRendererManager(VariableRendererManager variableRendererManager) {
    this.variableRendererManager = variableRendererManager;
  }
  
  public VariableRendererManager getVariableRendererManager() {
    return variableRendererManager;
  }
  
  public WorkflowDefinitionConversionFactory getWorkflowDefinitionConversionFactory() {
    return workflowDefinitionConversionFactory;
  }
  
  public void setLocale(Locale locale) {
    super.setLocale(locale);
    if(i18nManager != null) {
      i18nManager.setLocale(locale);
    }
  }
  
  // HttpServletRequestListener -------------------------------------------------------------------
  
  public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
    // Set current application object as thread-local to make it easy accessible
    current.set(this);
    
    // Authentication: check if user is found, otherwise send to login page
    LoggedInUser user = (LoggedInUser) getUser();
    if (user == null) {
      // First, try automatic login
      user = loginHandler.authenticate(request, response);
      if(user == null) {
        if (mainWindow != null && !mainWindow.isShowingLoginPage()) {
          viewManager.showLoginPage();
        }
      } else {
        setUser(user);
      }
    } 

    if(user != null) {
      Authentication.setAuthenticatedUserId(user.getId());
      if (mainWindow != null && mainWindow.isShowingLoginPage()) {
        viewManager.showDefaultPage();
      }
    }
    
    // Callback to the login handler
    loginHandler.onRequestStart(request, response);
  }
  
  public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
    // Clean up thread-local app
    current.remove();
    
    // Clear authentication context
    Authentication.setAuthenticatedUserId(null);
    
    // Callback to the login handler
    loginHandler.onRequestEnd(request, response);
    
    if(!isRunning() && !invalidatedSession) {
      // Clear the session context, the application has been closed during this request, otherwise
      // the application will be stuck on the spring-session scope and will be reused on the next
      // request, which will lead to problems
      if(request.getSession(false) != null) {
        request.getSession().invalidate();
        invalidatedSession = true;
      }
    }
  }
  
  // Error handling ---------------------------------------------------------------------------------
  
  @Override
  public void terminalError(com.vaadin.terminal.Terminal.ErrorEvent event) {
    super.terminalError(event);
    
    // Look for an Activiti Exception, as it'll probably be more meaningful.
    // If not found, just show default
    Throwable exception = event.getThrowable().getCause();
    int depth = 0; // To avoid going too deep in the stack
    while (exception != null && depth<20 && !(exception instanceof ActivitiException)) {
      exception = exception.getCause();
      depth++;
    }
    
    if (exception == null) {
      exception = event.getThrowable().getCause();
    }
    notificationManager.showErrorNotification(Messages.UNCAUGHT_EXCEPTION, exception.getMessage());
  }
  
  // URL Handling ---------------------------------------------------------------------------------
  
  public void setCurrentUriFragment(UriFragment fragment) {
    mainWindow.setCurrentUriFragment(fragment);
  }
  public UriFragment getCurrentUriFragment() {
    return mainWindow.getCurrentUriFragment();
  }
  
  // Injection setters
  
  public void setEnvironment(String environment) {
    this.environment = environment;
  }
  public boolean isUseJavascriptDiagram() {
    return useJavascriptDiagram;
  }
  public void setUseJavascriptDiagram(boolean useJavascriptDiagram) {
    this.useJavascriptDiagram = useJavascriptDiagram;
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
  public void setWorkflowDefinitionConversionFactory(WorkflowDefinitionConversionFactory workflowDefinitionConversionFactory) {
    this.workflowDefinitionConversionFactory = workflowDefinitionConversionFactory;
  }
  public List<String> getAdminGroups() {
    return adminGroups;
  }
  public void setAdminGroups(List<String> adminGroups) {
    this.adminGroups = adminGroups;
  }
  public List<String> getUserGroups() {
    return userGroups;
  }
  public void setUserGroups(List<String> userGroups) {
    this.userGroups = userGroups;
  }
  public SimpleWorkflowJsonConverter getSimpleWorkflowJsonConverter() {
	  return simpleWorkflowJsonConverter;
  }
  public void setSimpleWorkflowJsonConverter(SimpleWorkflowJsonConverter simpleWorkflowJsonConverter) {
	  this.simpleWorkflowJsonConverter = simpleWorkflowJsonConverter;
  }

  public String getCrystalBallCurrentDefinitionId() {
    return crystalBallCurrentDefinitionId;
  }

  public void setCrystalBallCurrentDefinitionId(String crystalBallCurrentDefinitionId) {
    this.crystalBallCurrentDefinitionId = crystalBallCurrentDefinitionId;
  }

  public String getCrystalBallCurrentInstanceId() {
    return crystalBallCurrentInstanceId;
  }

  public void setCrystalBallCurrentInstanceId(String crystalBallCurrentInstanceId) {
    this.crystalBallCurrentInstanceId = crystalBallCurrentInstanceId;
  }

  public List<SimulationEvent> getCrystalBallSimulationEvents() {
    return crystalBallSimulationEvents;
  }

  public void setCrystalBallSimulationEvents(List<SimulationEvent> crystalBallSimulationEvents) {
    this.crystalBallSimulationEvents = crystalBallSimulationEvents;
  }

  public SimulationDebugger getCrystalBallSimulationDebugger() {
    return crystalBallSimulationDebugger;
  }

  public void setCrystalBallSimulationDebugger(SimulationDebugger crystalBallSimulationDebugger) {
    this.crystalBallSimulationDebugger = crystalBallSimulationDebugger;
  }
  
}
