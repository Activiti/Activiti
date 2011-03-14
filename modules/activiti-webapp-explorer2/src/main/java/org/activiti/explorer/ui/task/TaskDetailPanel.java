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
import org.activiti.explorer.ui.profile.ProfilePage;

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
public class TaskDetailPanel extends Panel {
  
  private static final long serialVersionUID = -2018798598805436750L;
  
  protected ViewManager viewManager;
  protected TaskService taskService;
  protected Task task;
  
  public TaskDetailPanel(ViewManager viewManager, String taskId) {
    super();
    setSizeFull();
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    this.viewManager = viewManager;
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.task = taskService.createTaskQuery().taskId(taskId).singleResult();
    
    initName();
    initDescription();
    initTimeDetails();
    initPeopleDetails();
  }
  
  protected void initName() {
    Label nameLabel = new Label(task.getName());
    nameLabel.addStyleName(Reindeer.LABEL_H1);
    addComponent(nameLabel);
  }
  
  protected void initDescription() {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    addComponent(emptySpace);
    
    if (task.getDescription() != null) {
      Label descriptionLabel = new Label(task.getDescription());
      descriptionLabel.addStyleName(Reindeer.LABEL_SMALL);
      addComponent(descriptionLabel);
    }
    
    emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    addComponent(emptySpace);
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
    addComponent(timeDetailsLayout);

    Embedded clockImage = new Embedded(null, viewManager.getThemeResource(Constants.IMAGE_CLOCK));
    timeDetailsLayout.addComponent(clockImage);

    // The other time fields are layed out in a 2 column grid
    GridLayout grid = new GridLayout();
    grid.addStyleName(Constants.STYLE_TASK_DETAILS);
    grid.setSpacing(true);
    grid.setColumns(2);
    timeDetailsLayout.addComponent(grid);

    // create time
    if (task.getCreateTime() != null) {
      Label createTime = new Label("Created on: ");
      createTime.addStyleName(Constants.STYLE_LABEL_BOLD);
      createTime.setSizeUndefined();
      grid.addComponent(createTime);

      Label createTimeValue = new Label(Constants.DEFAULT_DATE_FORMATTER.format(task.getCreateTime()));
      createTimeValue.setSizeUndefined();
      grid.addComponent(createTimeValue);
    }

    // due date
    if (task.getDueDate() != null) {
      Label dueDate = new Label("Finish before:");
      dueDate.addStyleName(Constants.STYLE_LABEL_BOLD);
      dueDate.setSizeUndefined();
      grid.addComponent(dueDate);

      Label dueDateLabel = new Label(Constants.DEFAULT_DATE_FORMATTER.format(task.getDueDate()));
      dueDateLabel.setSizeUndefined();
      grid.addComponent(dueDateLabel);
    }
  }

  protected void initPeopleDetails() {
    // first add some empty space for aesthetics
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    addComponent(emptySpace);
    
    // Layout for involved people
    HorizontalLayout layout = new HorizontalLayout();
    layout.setSpacing(true);
    layout.setSizeUndefined();
    addComponent(layout);
    
    // people icon
    Embedded peopleImage = new Embedded(null, viewManager.getThemeResource(Constants.IMAGE_PEOPLE));
    layout.addComponent(peopleImage);
    
    // The involved people are layed out in a grid with two rows
    GridLayout grid = new GridLayout();
    grid.addStyleName(Constants.STYLE_TASK_DETAILS);
    grid.setSpacing(true);
    grid.setRows(2);
    layout.addComponent(grid);
    
    // owner
    if (task.getOwner() != null) {
      Button owner = new Button(task.getOwner() + " (owner)");
      owner.addStyleName(Reindeer.BUTTON_LINK);
      owner.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          viewManager.switchView(Constants.VIEW_PROFILE, new ProfilePage(viewManager, task.getOwner()));
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
          viewManager.switchView(Constants.VIEW_PROFILE, new ProfilePage(viewManager, task.getAssignee()));
        }
      });
      grid.addComponent(assignee);
    }
  }

}
