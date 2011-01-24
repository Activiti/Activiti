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
package org.activiti.kickstart.ui;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.activiti.kickstart.KickStartApplication;

import com.vaadin.ui.Panel;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.Window;

/**
 * Based on
 * http://dev.vaadin.com/browser/incubator/gasdiary/src/org/vaadin/gasdiary
 * /ui/ViewManager.java
 * 
 * @author Joram Barrez
 */
public class ViewManager implements Serializable {

  private static final long serialVersionUID = 4097162454884471228L;
  
  public static final String EDIT_ADHOC_WORKFLOW = "editAdhocWorkflow";
  public static final String PROCESS_SUCESSFULLY_DEPLOYED = "processSuccessfullyDeployed";
  public static final String SELECT_ADHOC_WORKFLOW = "selectAdhocWorkflow";

  protected KickStartApplication application;
  protected SplitPanel splitPanel;
  protected Map<String, Panel> views = new HashMap<String, Panel>();
  protected Stack<Panel> screenStack = new Stack<Panel>();

  public ViewManager(KickStartApplication application, SplitPanel splitPanel) {
    this.application = application;
    this.splitPanel = splitPanel;
  }

  public void switchWorkArea(String viewName, Panel workAreaPanel) {
    Panel panel = workAreaPanel;
    if (workAreaPanel != null) {
      views.put(viewName, workAreaPanel);
    } else {
      panel = views.get(viewName);
    }
    splitPanel.setSecondComponent(panel);
  }

  public void showPopupWindow(Window popupWindow) {
    application.getMainWindow().addWindow(popupWindow);
  }

  public void pushWorkArea(String viewName, Panel workAreaPanel) {
    screenStack.push((Panel) splitPanel.getSecondComponent());
    switchWorkArea(viewName, workAreaPanel);
  }

  public void popWorkArea() {
    splitPanel.setSecondComponent(screenStack.pop());
  }

  public KickStartApplication getApplication() {
    return application;
  }

}
