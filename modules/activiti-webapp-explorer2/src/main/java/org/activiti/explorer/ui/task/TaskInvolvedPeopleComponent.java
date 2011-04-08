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

import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;


/**
 * @author Joram Barrez
 */
public class TaskInvolvedPeopleComponent extends CustomComponent {

  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  
  protected Task task;
  protected VerticalLayout layout;
  
  public TaskInvolvedPeopleComponent(Task task) {
    this.task = task;
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    
    initUi();
  }
  
  protected void initUi() {
    initLayout();
    initHeader();
    initPeopleGrid();
  }
  
  protected void initLayout() {
    this.layout = new VerticalLayout();
    setCompositionRoot(layout);
  }
  
  protected void initHeader() {
    Label title = new Label(i18nManager.getMessage(Messages.TASK_PEOPLE));
    title.addStyleName(ExplorerLayout.STYLE_RELATED_CONTENT_DETAILS_HEADER); // TODO: make style generic
    layout.addComponent(title);
    
    initAddButton();
  }
  
  protected void initAddButton() {
    Embedded addPeopleButton = new Embedded(null, Images.ADD);
    addPeopleButton.addStyleName(ExplorerLayout.STYLE_IMAGE_ACTION);
    layout.addComponent(addPeopleButton);
    
    addPeopleButton.addListener(new ClickListener() {
      public void click(ClickEvent event) {
        viewManager.showPopupWindow(new InvolvePeoplePopupWindow());
      }
    });
  }
  
  protected void initPeopleGrid() {
    GridLayout grid = new GridLayout();
    grid.setColumns(2);
    grid.setSpacing(true);
    layout.addComponent(grid);
    
    // Owner
    String roleMessage = task.getOwner() != null ? Messages.TASK_OWNER : Messages.TASK_NO_OWNER;
    UserDetailsComponent ownerDetails = new UserDetailsComponent(task.getOwner(),
            i18nManager.getMessage(roleMessage));
    grid.addComponent(ownerDetails);
    
    // Assignee
    UserDetailsComponent assigneeDetails = new UserDetailsComponent(task.getAssignee(),
              i18nManager.getMessage(Messages.TASK_ASSIGNEE));
    grid.addComponent(assigneeDetails);
  }

}
