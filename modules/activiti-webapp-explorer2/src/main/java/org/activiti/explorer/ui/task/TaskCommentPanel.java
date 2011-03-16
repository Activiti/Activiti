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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Comment;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ui.ViewManager;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class TaskCommentPanel extends Panel {
  
  private static final long serialVersionUID = -1364956575106533335L;
  
  protected ViewManager viewManager;
  protected String taskId;
  protected TaskService taskService; 
  protected List<Comment> comments;
  protected List<TaskComment> taskComments; // ui-version of the comment

  public TaskCommentPanel(ViewManager viewManager, String taskId) {
    super();
    this.viewManager = viewManager;
    this.taskId = taskId;
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.comments = taskService.getTaskComments(taskId);
    
    setSizeFull();
    
    addTaskComments();
    addTextArea();
  }
  
  protected void addTaskComments() {
    this.taskComments = new ArrayList<TaskComment>();
    for (Comment comment : comments) {
      TaskComment taskComment = new TaskComment(viewManager, comment);
      taskComments.add(taskComment);
      addComponent(taskComment);
    }
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
