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
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class TaskEventsPanel extends Panel {
  
  private static final long serialVersionUID = 1L;
  
  protected IdentityService identityService;
  protected TaskService taskService; 
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  
  protected String taskId;
  protected List<org.activiti.engine.task.Event> taskEvents;

  public TaskEventsPanel(String taskId) {
    this.taskId = taskId;
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    
    setSizeFull();
    refreshTaskEvents();
  }
  
  protected void refreshTaskEvents() {
    removeAllComponents();
    this.taskEvents = taskService.getTaskEvents(taskId);
    
    addTaskEvents();
    addTextArea();
  }
  
  protected void addTaskEvents() {
    GridLayout eventGrid = new GridLayout();
    eventGrid.setColumns(2);
    eventGrid.setSpacing(true);
    eventGrid.setWidth("100%");
    eventGrid.setColumnExpandRatio(1, 1.0f);
    addComponent(eventGrid);
    
    for (final org.activiti.engine.task.Event event : taskEvents) {
      addTaskEventPicture(event, eventGrid);
      addTaskEventText(event, eventGrid);
    }
    
  }

  protected void addTaskEventPicture(final org.activiti.engine.task.Event taskEvent, GridLayout eventGrid) {
    final Picture userPicture = identityService.getUserPicture(taskEvent.getUserId());
    Embedded authorPicture = null;
    
    if (userPicture != null) {
      StreamResource imageresource = new StreamResource(new StreamSource() {
        private static final long serialVersionUID = 1L;
        public InputStream getStream() {
          return userPicture.getInputStream();
        }
      }, taskEvent.getUserId(), ExplorerApp.get());
      authorPicture = new Embedded(null, imageresource);
    } else {
      authorPicture = new Embedded(null, Images.USER_48);
    }
    
    authorPicture.setType(Embedded.TYPE_IMAGE);
    authorPicture.setHeight("48px");
    authorPicture.setWidth("48px");
    authorPicture.addStyleName(ExplorerLayout.STYLE_TASK_EVENT_PICTURE);
    eventGrid.addComponent(authorPicture);
  }
  
  protected void addTaskEventText(final org.activiti.engine.task.Event taskEvent, final GridLayout eventGrid) {
    VerticalLayout layout = new VerticalLayout();
    layout.addStyleName(ExplorerLayout.STYLE_TASK_EVENT);
    layout.setWidth("100%");
    eventGrid.addComponent(layout);
    
    // listener to show popup window with event details 
    layout.addListener(new LayoutClickListener() {
      public void layoutClick(LayoutClickEvent event) {
        viewManager.showTaskEventPopup(taskEvent);
      }
    });
    
    HorizontalLayout header = new HorizontalLayout();
    header.setSpacing(true);
    layout.addComponent(header);

    // Name
    User user = identityService.createUserQuery().userId(taskEvent.getUserId()).singleResult();
    Label name = new Label(user.getFirstName() + " " + user.getLastName());
    name.setSizeUndefined();
    name.addStyleName(ExplorerLayout.STYLE_TASK_EVENT_AUTHOR);
    header.addComponent(name);
    
    Label time = new Label(new PrettyTime().format(taskEvent.getTime()));
    time.setSizeUndefined();
    time.addStyleName(ExplorerLayout.STYLE_TASK_EVENT_TIME);
    header.addComponent(time);
    
    // Actual text
    Label text = new Label(taskEvent.getMessage());
    text.setWidth("100%");
    layout.addComponent(text);
    
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
    textArea.addStyleName(ExplorerLayout.STYLE_TASK_EVENT_TIME);
    grid.addComponent(textArea);
    
    Button addCommentButton = new Button(i18nManager.getMessage(Messages.TASK_ADD_COMMENT));
    addCommentButton.addStyleName(Reindeer.BUTTON_SMALL);
    grid.addComponent(addCommentButton);
    grid.setComponentAlignment(addCommentButton, Alignment.BOTTOM_RIGHT);
    addCommentButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        taskService.addComment(taskId, null, (String) textArea.getValue());
        refreshTaskEvents();
      }
    });
  }

}
