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

import java.io.InputStream;
import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.Comment;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.ui.ViewManager;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class TaskCommentPanel extends Panel {
  
  private static final long serialVersionUID = -1364956575106533335L;
  
  protected ViewManager viewManager;
  protected String taskId;
  protected TaskService taskService; 
  protected IdentityService identityService;
  protected List<Comment> comments;

  public TaskCommentPanel(ViewManager viewManager, String taskId) {
    super();
    this.viewManager = viewManager;
    this.taskId = taskId;
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.comments = taskService.getTaskComments(taskId);
    
    setSizeFull();
    refreshAllComments();
  }
  
  protected void addTaskComments() {
    GridLayout commentGrid = new GridLayout();
    commentGrid.setColumns(2);
    commentGrid.setWidth("100%");
    commentGrid.setColumnExpandRatio(1, 1.0f);
    addComponent(commentGrid);
    
    for (final Comment comment : comments) {
      addCommentPicture(comment, commentGrid);
      addCommentText(comment, commentGrid);
    }
    
  }

  protected void addCommentPicture(final Comment comment, GridLayout commentGrid) {
    StreamResource imageresource = new StreamResource(new StreamSource() {
      private static final long serialVersionUID = -8875067466181823014L;
      public InputStream getStream() {
        return identityService.getUserPicture(comment.getUserId()).getInputStream();
      }
    }, comment.getUserId(), viewManager.getApplication());
    
    Embedded picture = new Embedded("", imageresource);
    picture.setType(Embedded.TYPE_IMAGE);
    picture.setHeight("48px");
    picture.setWidth("48px");
    picture.addStyleName(Constants.STYLE_TASK_COMMENT_PICTURE);
    commentGrid.addComponent(picture);
  }
  
  protected void addCommentText(final Comment comment, final GridLayout commentGrid) {
    VerticalLayout layout = new VerticalLayout();
    layout.addStyleName(Constants.STYLE_TASK_COMMENT);
    layout.setWidth("100%");
    commentGrid.addComponent(layout);
    
    // listener to show popup window with comment 
    layout.addListener(new LayoutClickListener() {
      public void layoutClick(LayoutClickEvent event) {
//        viewManager.showPopupWindow(new TaskCommentPopupWindow(viewManager, comment));
      }
    });
    
    HorizontalLayout commentHeader = new HorizontalLayout();
    commentHeader.setSpacing(true);
    layout.addComponent(commentHeader);

    // Name
    User user = identityService.createUserQuery().userId(comment.getUserId()).singleResult();
    Label name = new Label(user.getFirstName() + " " + user.getLastName());
    name.setSizeUndefined();
    name.addStyleName(Constants.STYLE_TASK_COMMENT_AUTHOR);
    commentHeader.addComponent(name);
    
    Label time = new Label(new PrettyTime().format(comment.getTime()));
    time.setSizeUndefined();
    time.addStyleName(Constants.STYLE_TASK_COMMENT_TIME);
    commentHeader.addComponent(time);
    
    // Actual text
    Label text = null;
    if (comment.getMessage().length() < 140) {
      text = new Label(comment.getMessage());
    } else {
      text = new Label(comment.getMessage().substring(0, 140) + "...");
    }
    text.setWidth("100%");
    layout.addComponent(text);
    
  }

  
  protected void refreshAllComments() {
    removeAllComponents();
    this.comments = taskService.getTaskComments(taskId);
    
    addTaskComments();
    addTextArea();
  }
  
  protected void addTextArea() {
    addComponent(new Label("&nbsp", Label.CONTENT_XHTML));
    
    GridLayout grid = new GridLayout(1, 2);
    grid.setWidth("100%");
    grid.setSpacing(true);
    addComponent(grid);
    
    final TextArea textArea = new TextArea();
    textArea.setRows(2);
    textArea.setWidth("100%");
    textArea.addStyleName(Constants.STYLE_TASK_COMMENT_TIME);
    grid.addComponent(textArea);
    
    Button addCommentButtom = new Button("Add comment");
    addCommentButtom.addStyleName(Reindeer.BUTTON_SMALL);
    grid.addComponent(addCommentButtom);
    grid.setComponentAlignment(addCommentButtom, Alignment.BOTTOM_RIGHT);
    addCommentButtom.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        taskService.addComment(taskId, null, (String) textArea.getValue());
        refreshAllComments();
      }
    });
  }

}
