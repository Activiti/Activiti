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

import java.io.Serializable;
import java.util.HashMap;

import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApplication;

import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Window;

/**
 * Based on a best practice pattern described at
 * http://dev.vaadin.com/browser/incubator/gasdiary/src/org/vaadin/gasdiary/ui/ViewManager.java
 * 
 * @author Joram Barrez
 */
public class ViewManager implements Serializable {

  private static final long serialVersionUID = 4097162454884471228L;
  
  protected ExplorerApplication application;
  protected CustomLayout mainLayout;
  
  // Cache for views
  protected HashMap<String, Component> views = new HashMap<String, Component>();
  
  public ViewManager(ExplorerApplication application, CustomLayout mainLayout) {
    this.application = application;
    this.mainLayout = mainLayout;
  }

  public void switchView(String viewName, Component component) {
    Component newView = null;
    if (component != null) {
      newView = component;
      views.put(viewName, component);
    } else {
      newView = views.get(viewName);
    }
    
    mainLayout.addComponent(newView, Constants.LOCATION_CONTENT); // despite the method name, the old component is actually removed before the new one is added
  }

  public void showPopupWindow(Window popupWindow) {
    application.getMainWindow().addWindow(popupWindow);
  }

  public ExplorerApplication getApplication() {
    return application;
  }
  
  public Resource getThemeResource(String resourceName) {
    return new ThemeResource(resourceName);
  }
  
  public Resource getClassResource(String resourceName) {
    return new ClassResource(resourceName, application);
  }

}
