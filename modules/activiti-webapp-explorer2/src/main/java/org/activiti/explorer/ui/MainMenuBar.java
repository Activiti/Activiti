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

import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.ui.flow.FlowPage;
import org.activiti.explorer.ui.management.DatabasePage;
import org.activiti.explorer.ui.task.TaskInboxPage;
import org.activiti.explorer.ui.task.TaskPage;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Joram Barrez
 */
@SuppressWarnings("serial")
public class MainMenuBar extends MenuBar {
  
  private static final long serialVersionUID = 5545216112926052463L;
  
  public MainMenuBar() {
    super();
    
    Button taskButton = createMenuBarButton("Tasks");
    taskButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        ExplorerApplication.getCurrent().switchView(new TaskInboxPage());
      }
    });
    
    Button flowButton = createMenuBarButton("Flows");
    flowButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        ExplorerApplication.getCurrent().switchView(new FlowPage());
      }
    });
    
    Button managementButton = createMenuBarButton("Management");
    managementButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        ExplorerApplication.getCurrent().switchView(new DatabasePage());
      }
    });
    
    
    createMenuBarButton("Reports");
    fillRemainingSpace();
  }
  
}
