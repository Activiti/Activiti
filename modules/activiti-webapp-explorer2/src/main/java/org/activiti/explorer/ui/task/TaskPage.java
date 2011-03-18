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

package org.activiti.explorer.ui.task;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class TaskPage extends CustomComponent {
  
  private static final long serialVersionUID = 2310017323549425167L;
  
  // services
  protected TaskService taskService;
  
  // ui
  protected VerticalLayout taskPageLayout;
  protected HorizontalSplitPanel mainSplitPanel;
  protected Table taskTable;
  
  public TaskPage() {
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    
    addTaskPageLayout();
    addTaskMenuBar();
    addMainSplitPanel();
  }

  private void addTaskPageLayout() {
    // The main layout of this page is a vertical layout:
    // on top there is the dynamic task menu bar, on the bottom the rest
    taskPageLayout = new VerticalLayout();
    taskPageLayout.setSizeFull();
    setCompositionRoot(taskPageLayout);
  }

  protected void addMainSplitPanel() {
    // The actual content of the page is a HorizontalSplitPanel,
    // with on the left side the task list
    mainSplitPanel = new HorizontalSplitPanel();
    mainSplitPanel.addStyleName(Reindeer.SPLITPANEL_SMALL);
    mainSplitPanel.setSizeFull();
    mainSplitPanel.setSplitPosition(17, HorizontalSplitPanel.UNITS_PERCENTAGE);
    taskPageLayout.addComponent(mainSplitPanel);
    taskPageLayout.setExpandRatio(mainSplitPanel, 1.0f);
  }
  
  protected void addTaskMenuBar() {
    TaskMenuBar taskMenuBar = new TaskMenuBar();
    taskPageLayout.addComponent(taskMenuBar);
  }
  
}
