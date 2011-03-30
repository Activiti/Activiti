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
package org.activiti.kickstart;

import org.activiti.kickstart.ui.ViewManager;
import org.activiti.kickstart.ui.panel.ActionsPanel;
import org.activiti.kickstart.ui.panel.KickstartWorkflowPanel;

import com.vaadin.Application;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Joram Barrez
 */
public class KickStartApplication extends Application {

  protected static final long serialVersionUID = 6197397757268207621L;

  protected static final String TITLE = "Activiti KickStart";
  protected static final String THEME_NAME = "yakalo";
  protected static final String CONTENT_LOCATION = "content"; // id of div in layout where app needs to come

  // ui
  protected ViewManager viewManager;
  protected CustomLayout mainLayout; // general layout of the app
  protected HorizontalSplitPanel splitPanel; // app uses a split panel: left actions, right work area
  protected ActionsPanel actionsPanel; // left panel with user actions
  protected Panel currentWorkArea; // right panel of ui where actual work happens

  public void init() {
    setTheme(THEME_NAME);
    initMainWindow();
    initDefaultWorkArea();
  }

  protected void initMainWindow() {

    Window mainWindow = new Window(TITLE);
    setMainWindow(mainWindow);
    Panel p = new Panel();
    p.setSizeFull();
    mainWindow.setContent(p);
    
    mainLayout = new CustomLayout(THEME_NAME); // uses layout defined in webapp/Vaadin/themes/yakalo
    mainLayout.setSizeFull();
    p.setContent(mainLayout);

    initSplitPanel();
    initViewManager();
    initActionsPanel();
  }

  protected void initSplitPanel() {
    splitPanel = new HorizontalSplitPanel(); 
    splitPanel.setSplitPosition(170, HorizontalSplitPanel.UNITS_PIXELS);
    splitPanel.setStyleName(Reindeer.LAYOUT_WHITE);
    splitPanel.setSizeFull();
    
    mainLayout.addComponent(splitPanel, CONTENT_LOCATION);
  }

  protected void initActionsPanel() {
    this.actionsPanel = new ActionsPanel(viewManager);
    splitPanel.setFirstComponent(actionsPanel);
  }

  protected void initViewManager() {
    this.viewManager = new ViewManager(this, splitPanel);
  }

  protected void initDefaultWorkArea() {
    viewManager.switchWorkArea(ViewManager.EDIT_ADHOC_WORKFLOW, new KickstartWorkflowPanel(viewManager));
  }

  // GETTERS /////////////////////////////////////////////////////////////////////////

  public ViewManager getViewManager() {
    return viewManager;
  }

}
