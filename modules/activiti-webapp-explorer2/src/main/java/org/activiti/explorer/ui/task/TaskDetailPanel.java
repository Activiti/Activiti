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
import org.activiti.engine.task.Task;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ui.ViewManager;
import org.activiti.explorer.ui.profile.ProfilePopupWindow;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class TaskDetailPanel extends HorizontalLayout {
  
  private static final long serialVersionUID = -2018798598805436750L;
  
  protected ViewManager viewManager;
  protected TaskService taskService;
  protected Task task;
  
  protected Panel leftPanel;
  protected Panel rightPanel;
  
  public TaskDetailPanel(ViewManager viewManager, String taskId) {
    super();
    this.viewManager = viewManager;
    setSizeFull();
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.task = taskService.createTaskQuery().taskId(taskId).singleResult();
    
    // left panel: all details about the task
    this.leftPanel = new Panel();
    leftPanel.addStyleName(Reindeer.PANEL_LIGHT);
    addComponent(leftPanel);
    setExpandRatio(leftPanel, 75.0f);
    
    initName();
    initDescription();
    initTimeDetails();
    initPeopleDetails();
    
    
    // Right panel: the task comments
    this.rightPanel = new TaskCommentPanel(viewManager, taskId);
    rightPanel.addStyleName(Reindeer.PANEL_LIGHT);
    addComponent(rightPanel);
    setExpandRatio(rightPanel, 25.0f);
  }
  
  protected void initName() {
    Label nameLabel = new Label(task.getName());
    nameLabel.addStyleName(Reindeer.LABEL_H1);
    leftPanel.addComponent(nameLabel);
  }
  
  protected void initDescription() {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    leftPanel.addComponent(emptySpace);
    
    if (task.getDescription() != null) {
      Label descriptionLabel = new Label(task.getDescription());
      descriptionLabel.addStyleName(Reindeer.LABEL_SMALL);
      leftPanel.addComponent(descriptionLabel);
    }
    
    emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    leftPanel.addComponent(emptySpace);
  }

  protected void initTimeDetails() {
    // Label detailsHeader = new Label("Details");
    // detailsHeader.addStyleName(Reindeer.LABEL_H2);
    // addComponent(detailsHeader);
    // Label horizontalLine = new Label("<hr />", Label.CONTENT_XHTML);
    // horizontalLine.addStyleName(Constants.STYLE_HORIZONTAL_SEPARATOR);
    // addComponent(horizontalLine);

    HorizontalLayout timeDetailsLayout = new HorizontalLayout();
    timeDetailsLayout.setSpacing(true);
    timeDetailsLayout.setSizeUndefined();
    leftPanel.addComponent(timeDetailsLayout);

    Embedded clockImage = new Embedded(null, new ThemeResource(Constants.IMAGE_CLOCK));
    timeDetailsLayout.addComponent(clockImage);

    // The other time fields are layed out in a 2 column grid
    GridLayout grid = new GridLayout();
    grid.addStyleName(Constants.STYLE_TASK_DETAILS);
    grid.setSpacing(true);
    grid.setColumns(2);

    timeDetailsLayout.addComponent(grid);
    timeDetailsLayout.setComponentAlignment(grid, Alignment.MIDDLE_LEFT);

    // create time
    if (task.getCreateTime() != null) {
      Label createTime = new Label("Created " + new PrettyTime().format(task.getCreateTime()));
      createTime.addStyleName(Constants.STYLE_LABEL_BOLD);
      createTime.setSizeUndefined();
      grid.addComponent(createTime);
      
      Label realCreateTime = new Label("(" + task.getCreateTime() + ")");
      realCreateTime.addStyleName(Reindeer.LABEL_SMALL);
      realCreateTime.setSizeUndefined();
      grid.addComponent(realCreateTime);
    }

    // due date
    if (task.getDueDate() != null) {
      Label dueDate = new Label("Has to be finished " + new PrettyTime().format(task.getDueDate())); 
      dueDate.addStyleName(Constants.STYLE_LABEL_BOLD);
      dueDate.setSizeUndefined();
      grid.addComponent(dueDate);

      Label realDueDateTime = new Label("(" + task.getDueDate() + ")");
      realDueDateTime.addStyleName(Reindeer.LABEL_SMALL);
      realDueDateTime.setSizeUndefined();
      grid.addComponent(realDueDateTime);
    }
  }

  protected void initPeopleDetails() {
    // first add some empty space for aesthetics
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    leftPanel.addComponent(emptySpace);
    
    // Layout for involved people
    HorizontalLayout layout = new HorizontalLayout();
    layout.setSpacing(true);
    layout.setSizeUndefined();
    leftPanel.addComponent(layout);
    
    // people icon
    Embedded peopleImage = new Embedded(null, new ThemeResource(Constants.IMAGE_PEOPLE));
    layout.addComponent(peopleImage);
    
    // The involved people are layed out in a grid with two rows
    GridLayout grid = new GridLayout();
    grid.addStyleName(Constants.STYLE_TASK_DETAILS);
    grid.setSpacing(true);
    grid.setRows(2);
    
    layout.addComponent(grid);
    layout.setComponentAlignment(grid, Alignment.MIDDLE_LEFT);
    
    // owner
    if (task.getOwner() != null) {
      Button owner = new Button(task.getOwner() + " (owner)");
      owner.addStyleName(Reindeer.BUTTON_LINK);
      owner.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          viewManager.showPopupWindow(new ProfilePopupWindow(viewManager, task.getOwner()));
        }
      });
      
      grid.addComponent(owner);
    }
    
    // assignee
    if (task.getAssignee() != null) {
      Button assignee = new Button(task.getAssignee() + " (assignee)");
      assignee.addStyleName(Reindeer.BUTTON_LINK);
      assignee.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          viewManager.showPopupWindow(new ProfilePopupWindow(viewManager, task.getAssignee()));
        }
      });
      grid.addComponent(assignee);
    }
  }
  
}
