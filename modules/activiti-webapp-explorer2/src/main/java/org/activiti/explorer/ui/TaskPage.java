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

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class TaskPage extends CustomComponent {
  
  private static final long serialVersionUID = 2310017323549425167L;
  
  protected ViewManager viewManager;
  protected VerticalLayout mainLayout;
  protected HorizontalSplitPanel mainSplitPanel;
  
  public TaskPage(ViewManager viewManager) {
    this.viewManager = viewManager;
    
    // The main layout of this page is a vertical layout:
    // on top there is the dynamic task menu bar, on the bottom the rest
    mainLayout = new VerticalLayout();
    mainLayout.setSizeFull();
    setCompositionRoot(mainLayout);
    setSizeFull();
    
    initTaskMenuBar();
    
    // The actual content of the page is a HorizontalSplitPanel,
    // with on the left side the task list
    mainSplitPanel = new HorizontalSplitPanel();
    mainSplitPanel.addStyleName(Reindeer.SPLITPANEL_SMALL);
    mainSplitPanel.setSizeFull();
    mainSplitPanel.setSplitPosition(20, HorizontalSplitPanel.UNITS_PERCENTAGE);
    mainLayout.addComponent(mainSplitPanel);
    mainLayout.setExpandRatio(mainSplitPanel, 1.0f);
   
    initTaskList();
  }
  
  protected void initTaskMenuBar() {
    TaskMenuBar taskMenuBar = new TaskMenuBar(viewManager);
    mainLayout.addComponent(taskMenuBar);
  }
  
  protected void initTaskList() {
    mainSplitPanel.setFirstComponent(new Label("Bliep"));
  }

}
