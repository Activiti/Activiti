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
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.event.SubmitEvent;
import org.activiti.explorer.ui.event.SubmitEventListener;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;


/**
 * @author Joram Barrez
 */
public class TaskInvolvedPeopleComponent extends CustomComponent {

  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  protected TaskService taskService;
  
  protected Task task;
  protected VerticalLayout layout;
  protected Label title;
  protected Embedded addPeopleButton;
  protected GridLayout peopleGrid;
  
  public TaskInvolvedPeopleComponent(Task task) {
    this.task = task;
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    
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
    
    layout.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML)); // TODO: remove
    
    HorizontalLayout headerLayout = new HorizontalLayout();
    headerLayout.setWidth(100, UNITS_PERCENTAGE);
    layout.addComponent(headerLayout);
    
    // Title
    title = new Label(i18nManager.getMessage(Messages.TASK_PEOPLE));
    title.addStyleName(ExplorerLayout.STYLE_RELATED_CONTENT_DETAILS_HEADER); // TODO: make style generic
    title.setWidth(100, UNITS_PERCENTAGE);
    headerLayout.addComponent(title);
    headerLayout.setExpandRatio(title, 1.0f);

    // Add button
    addPeopleButton = new Embedded(null, Images.ADD);
    addPeopleButton.addStyleName(ExplorerLayout.STYLE_IMAGE_ACTION);
    headerLayout.addComponent(addPeopleButton);
    
    addPeopleButton.addListener(new ClickListener() {
      public void click(ClickEvent event) {
        InvolvePeoplePopupWindow involvePeoplePopupWindow = new InvolvePeoplePopupWindow(task);
        involvePeoplePopupWindow.addListener(new SubmitEventListener() {
          protected void submitted(SubmitEvent event) {
            // Receiving this event means that some new people were involved, so we refresh 
            refreshPeopleGrid();
          }
          protected void cancelled(SubmitEvent event) {
          }
        });
        
        viewManager.showPopupWindow(involvePeoplePopupWindow);
      }
    });
  }
  
  protected void initPeopleGrid() {
    peopleGrid = new GridLayout();
    peopleGrid.setColumns(2);
    peopleGrid.setSpacing(true);
    
    populatePeopleGrid();
    layout.addComponent(peopleGrid);
  }
  
  protected void populatePeopleGrid() {
    // Owner
    String roleMessage = task.getOwner() != null ? Messages.TASK_OWNER : Messages.TASK_NO_OWNER;
    UserDetailsComponent ownerDetails = new UserDetailsComponent(task.getOwner(),
            i18nManager.getMessage(roleMessage));
    peopleGrid.addComponent(ownerDetails);
    
    // Assignee
    UserDetailsComponent assigneeDetails = new UserDetailsComponent(task.getAssignee(),
              i18nManager.getMessage(Messages.TASK_ASSIGNEE));
    peopleGrid.addComponent(assigneeDetails);
    
    // Other involved people
    for (IdentityLink identityLink : taskService.getIdentityLinksForTask(task.getId())) { 
      if (identityLink.getUserId() != null) { // only user identity links, ignoring the group ids
        if (!IdentityLinkType.ASSIGNEE.equals(identityLink.getType())) {
          UserDetailsComponent involvedDetails = new UserDetailsComponent(identityLink.getUserId(), identityLink.getType());
          peopleGrid.addComponent(involvedDetails);
        }
      }
    }
  }
  
  protected void refreshPeopleGrid() {
    peopleGrid.removeAllComponents();
    populatePeopleGrid();
  }

}
