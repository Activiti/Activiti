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
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.util.time.HumanTime;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * Component containing all events for a given task.
 * 
 * @author Joram Barrez
 */
public class TaskEventsPanel extends Panel {
  
  private static final long serialVersionUID = 1L;
  
  protected transient IdentityService identityService;
  protected transient TaskService taskService; 
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  protected TaskEventTextResolver taskEventTextResolver;

  protected String taskId;
  protected List<org.activiti.engine.task.Event> taskEvents;
  protected TextField commentInputField;
  protected Button addCommentButton;
  protected GridLayout eventGrid;

  public TaskEventsPanel() {
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    this.taskEventTextResolver = new TaskEventTextResolver();
    
    ((VerticalLayout) getContent()).setSpacing(true);
    ((VerticalLayout) getContent()).setMargin(true);
    setHeight(100, UNITS_PERCENTAGE);
    
    addStyleName(ExplorerLayout.STYLE_TASK_EVENT_PANEL);
    
    addTitle();
    addInputField();
    initEventGrid();
    addTaskEvents();
  }
  

  public void refreshTaskEvents() {
    eventGrid.removeAllComponents();
    addTaskEvents();
  }
  
  /**
   * Set the task this component is showing the events for. Triggers
   * an update of the UI.
   */
  public void setTaskId(String taskId) {
    this.taskId = taskId;
    refreshTaskEvents();
  }
  
  protected void addTitle() {
    Label eventTitle = new Label(i18nManager.getMessage(Messages.EVENT_TITLE));
    eventTitle.addStyleName(Reindeer.LABEL_H2);
    addComponent(eventTitle);
  }
  
  protected void initEventGrid() {
    eventGrid = new GridLayout();
    eventGrid.setColumns(2);
    eventGrid.setSpacing(true);
    eventGrid.setMargin(true, false, false, false);
    eventGrid.setWidth("100%");
    eventGrid.setColumnExpandRatio(1, 1.0f);
    eventGrid.addStyleName(ExplorerLayout.STYLE_TASK_EVENT_GRID);
    
    addComponent(eventGrid);
  }
  
  protected void addTaskEvents() {
    if(taskId != null) {
      taskEvents = taskService.getTaskEvents(taskId);
      for (final org.activiti.engine.task.Event event : taskEvents) {
        addTaskEventPicture(event, eventGrid);
        addTaskEventText(event, eventGrid);
      }
    }
    addCommentButton.setEnabled(taskId != null);
    commentInputField.setEnabled(taskId != null);
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
      }, "event_" + taskEvent.getUserId() + "." + Constants.MIMETYPE_EXTENSION_MAPPING.get(userPicture.getMimeType()), ExplorerApp.get());
      authorPicture = new Embedded(null, imageresource);
    } else {
      authorPicture = new Embedded(null, Images.USER_50);
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
    
    // Actual text
    Label text = taskEventTextResolver.resolveText(taskEvent);
    text.setWidth("100%");
    layout.addComponent(text);
    
    // Time
    Label time = new Label(new HumanTime(i18nManager).format(taskEvent.getTime()));
    time.setSizeUndefined();
    time.addStyleName(ExplorerLayout.STYLE_TASK_EVENT_TIME);
    layout.addComponent(time);
    
  }
  
  protected void addInputField() {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setSpacing(true);
    layout.setWidth(100, UNITS_PERCENTAGE);
    addComponent(layout);
    
    Panel textFieldPanel = new Panel(); // Hack: actionHandlers can only be attached to panels or windows
    textFieldPanel.addStyleName(Reindeer.PANEL_LIGHT);
    textFieldPanel.setContent(new VerticalLayout());
    textFieldPanel.setWidth(100, UNITS_PERCENTAGE);
    layout.addComponent(textFieldPanel);
    layout.setExpandRatio(textFieldPanel, 1.0f);
    
    commentInputField = new TextField();
    commentInputField.setWidth(100, UNITS_PERCENTAGE);
    textFieldPanel.addComponent(commentInputField);
    
    // Hack to catch keyboard 'enter'
    textFieldPanel.addActionHandler(new Handler() {
      public void handleAction(Action action, Object sender, Object target) {
        addNewComment(commentInputField.getValue().toString());
      }
      public Action[] getActions(Object target, Object sender) {
        return new Action[] {new ShortcutAction("enter", ShortcutAction.KeyCode.ENTER, null)};
      }
    });
    
    addCommentButton = new Button(i18nManager.getMessage(Messages.TASK_ADD_COMMENT));
    layout.addComponent(addCommentButton);
    layout.setComponentAlignment(addCommentButton, Alignment.MIDDLE_LEFT);
    addCommentButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        addNewComment(commentInputField.getValue().toString());
      }
    });
  }
  
  protected void addNewComment(String text) {
    taskService.addComment(taskId, null, text);
    refreshTaskEvents();
    commentInputField.setValue("");
    commentInputField.focus();
  }

}
