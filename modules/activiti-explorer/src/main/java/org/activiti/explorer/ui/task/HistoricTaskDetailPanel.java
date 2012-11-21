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

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.content.AttachmentDetailPopupWindow;
import org.activiti.explorer.ui.content.AttachmentRenderer;
import org.activiti.explorer.ui.content.AttachmentRendererManager;
import org.activiti.explorer.ui.content.RelatedContentComponent;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.custom.PrettyTimeLabel;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * A simplified version of the {@Link TaskDetailPanel}, displaying
 * read-only historic task details.
 * 
 * @author Joram Barrez
 */
public class HistoricTaskDetailPanel extends DetailPanel {
  
  private static final long serialVersionUID = 1L;
  
  protected HistoricTaskInstance historicTask;
  
  // Services
  protected HistoryService historyService;
  protected TaskService taskService;
  protected ViewManager viewManager;
  protected I18nManager i18nManager;
  protected NotificationManager notificationManager;
  protected AttachmentRendererManager attachmentRendererManager;
  
  // UI
  protected TaskPage taskPage;
  protected VerticalLayout centralLayout;
  protected VerticalLayout peopleLayout;
  protected GridLayout peopleGrid;
  protected VerticalLayout subTasksLayout;
  protected GridLayout subTaskGrid;
  protected VerticalLayout relatedContentLayout;
  
  public HistoricTaskDetailPanel(HistoricTaskInstance historicTask, TaskPage taskPage) {
    this.historicTask = historicTask;
    this.taskPage = taskPage;
    
    this.historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.viewManager = ExplorerApp.get().getViewManager();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.notificationManager = ExplorerApp.get().getNotificationManager();
    this.attachmentRendererManager = ExplorerApp.get().getAttachmentRendererManager();
  }
  
  @Override
  public void attach() {
    super.attach();
    init();
  }
  
  protected void init() {
    setSizeFull();
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    this.centralLayout = new VerticalLayout();
    centralLayout.setMargin(true);
    setDetailContainer(centralLayout);
    
    initHeader();
    initDescription();
    initParentTaskLink();
    initPeopleDetails();
    initSubTasks();
    initRelatedContent();
  }
  
  protected void initHeader() {
    GridLayout taskDetails = new GridLayout(5, 2);
    taskDetails.setWidth(100, UNITS_PERCENTAGE);
    taskDetails.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    taskDetails.setSpacing(true);
    taskDetails.setMargin(false, false, true, false);
    
    // Add image
    Embedded image = new Embedded(null, Images.TASK_50);
    taskDetails.addComponent(image, 0, 0, 0, 1);
    
    // Add task name
    Label nameLabel = new Label(historicTask.getName());
    nameLabel.addStyleName(Reindeer.LABEL_H2);
    taskDetails.addComponent(nameLabel, 1, 0, 4,0);

    // Add due date
    PrettyTimeLabel dueDateLabel = new PrettyTimeLabel(i18nManager.getMessage(Messages.TASK_DUEDATE_SHORT),
      historicTask.getDueDate(), i18nManager.getMessage(Messages.TASK_DUEDATE_UNKNOWN), false);
    dueDateLabel.addStyleName(ExplorerLayout.STYLE_TASK_HEADER_DUEDATE);
    taskDetails.addComponent(dueDateLabel, 1, 1);
    
    // Add priority
    Integer lowMedHighPriority = convertPriority(historicTask.getPriority());
    Label priorityLabel = new Label();
    switch(lowMedHighPriority) {
    case 1:
      priorityLabel.setValue(i18nManager.getMessage(Messages.TASK_PRIORITY_LOW));
      priorityLabel.addStyleName(ExplorerLayout.STYLE_TASK_HEADER_PRIORITY_LOW);
      break;
    case 2:
      priorityLabel.setValue(i18nManager.getMessage(Messages.TASK_PRIORITY_MEDIUM));
      priorityLabel.addStyleName(ExplorerLayout.STYLE_TASK_HEADER_PRIORITY_MEDIUM);
      break;
    case 3:
    default:
      priorityLabel.setValue(i18nManager.getMessage(Messages.TASK_PRIORITY_HIGH));
      priorityLabel.addStyleName(ExplorerLayout.STYLE_TASK_HEADER_PRIORITY_HIGH);
    }
    taskDetails.addComponent(priorityLabel, 2, 1);
    
    // Add create date
    PrettyTimeLabel createLabel = new PrettyTimeLabel(i18nManager.getMessage(Messages.TASK_CREATED_SHORT),
      historicTask.getStartTime(), "", true);
    createLabel.addStyleName(ExplorerLayout.STYLE_TASK_HEADER_CREATE_TIME);
    taskDetails.addComponent(createLabel, 3, 1);
    
    // Add label to fill excess space
    Label spacer = new Label();
    spacer.setContentMode(Label.CONTENT_XHTML);
    spacer.setValue("&nbsp;");
    spacer.setSizeUndefined();
    taskDetails.addComponent(spacer);
    
    taskDetails.setColumnExpandRatio(1, 1.0f);
    taskDetails.setColumnExpandRatio(2, 1.0f);
    taskDetails.setColumnExpandRatio(3, 1.0f);
    taskDetails.setColumnExpandRatio(4, 1.0f);
    centralLayout.addComponent(taskDetails);
  }
  
  protected void initDescription() {
    CssLayout descriptionLayout = new CssLayout();
    descriptionLayout.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    descriptionLayout.setWidth(100, UNITS_PERCENTAGE);
    
    if (historicTask.getDescription() != null) {
      Label descriptionLabel = new Label(historicTask.getDescription());
      descriptionLayout.addComponent(descriptionLabel);
    }
    
    centralLayout.addComponent(descriptionLayout);
  }

  protected void initParentTaskLink() {
    if (historicTask.getParentTaskId() != null) {
      final HistoricTaskInstance parentTask = historyService.createHistoricTaskInstanceQuery()
        .taskId(historicTask.getParentTaskId())
        .singleResult();
        
      Button showParentTaskButton = new Button(i18nManager.getMessage(
              Messages.TASK_SUBTASK_OF_PARENT_TASK, parentTask.getName()));
      showParentTaskButton.addStyleName(Reindeer.BUTTON_LINK);
      showParentTaskButton.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          viewManager.showTaskPage(parentTask.getId());
        }
      });
      
      centralLayout.addComponent(showParentTaskButton);
    }
  }
  
  protected void initPeopleDetails() {
    peopleLayout = new VerticalLayout();
    peopleLayout.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addComponent(peopleLayout);
    
    initPeopleTitle();
    initPeopleGrid();
    initOwner();
    initAssignee();
  }

  protected void initPeopleTitle() {
    Label title = new Label(i18nManager.getMessage(Messages.TASK_PEOPLE));
    title.addStyleName(ExplorerLayout.STYLE_H3);
    title.setWidth(100, UNITS_PERCENTAGE);
    peopleLayout.addComponent(title);
  }

  protected void initPeopleGrid() {
    peopleGrid = new GridLayout();
    peopleGrid.setColumns(2);
    peopleGrid.setSpacing(true);
    peopleGrid.setMargin(true, false, false, false);
    peopleGrid.setWidth(100, UNITS_PERCENTAGE);
    peopleLayout.addComponent(peopleGrid);
  }

  protected void initOwner() {
    String ownerRole = historicTask.getOwner() != null ? Messages.TASK_OWNER : Messages.TASK_NO_OWNER;
    UserDetailsComponent owner = new UserDetailsComponent(historicTask.getOwner(), i18nManager.getMessage(ownerRole));
    peopleGrid.addComponent(owner);
  }

  protected void initAssignee() {
    String assigneeRole = historicTask.getAssignee() != null ? Messages.TASK_ASSIGNEE : Messages.TASK_NO_ASSIGNEE;
    UserDetailsComponent assignee = new UserDetailsComponent(historicTask.getAssignee(), i18nManager.getMessage(assigneeRole));
    peopleGrid.addComponent(assignee);
  }
  
  
  protected void initSubTasks() {
    subTasksLayout = new VerticalLayout();
    subTasksLayout.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addComponent(subTasksLayout);
    initSubTaskTitle();
    
    List<HistoricTaskInstance> subTasks = historyService.createHistoricTaskInstanceQuery()
    .taskParentTaskId(historicTask.getId())
    .list();
    
    if (subTasks.size() > 0) {
      initSubTaskGrid();
      populateSubTasks(subTasks);
    } else {
      initNoSubTasksLabel();
    }
  }

  protected void initSubTaskTitle() {
    Label title = new Label(i18nManager.getMessage(Messages.TASK_SUBTASKS));
    title.addStyleName(ExplorerLayout.STYLE_H3);
    title.setWidth(100, UNITS_PERCENTAGE);
    subTasksLayout.addComponent(title);
  }

  protected void initSubTaskGrid() {
    subTaskGrid = new GridLayout();
    subTaskGrid.setColumns(2);
    subTasksLayout.addComponent(subTaskGrid);
  }

  protected void populateSubTasks(List<HistoricTaskInstance> subTasks) {
    for (final HistoricTaskInstance subTask : subTasks) {
      // icon
      Embedded icon = new Embedded(null, Images.TASK_22);
      icon.setWidth(22, UNITS_PIXELS);
      icon.setWidth(22, UNITS_PIXELS);
      subTaskGrid.addComponent(icon);
      
      // Link to subtask
      Button subTaskLink = new Button(subTask.getName());
      subTaskLink.addStyleName(Reindeer.BUTTON_LINK);
      subTaskLink.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          ExplorerApp.get().getViewManager().showTaskPage(subTask.getId());
        }
      });
      subTaskGrid.addComponent(subTaskLink);
      subTaskGrid.setComponentAlignment(subTaskLink, Alignment.MIDDLE_LEFT);
    }
  }
  
  protected void initNoSubTasksLabel() {
    Label noSubTasksLabel = new Label(i18nManager.getMessage(Messages.TASK_NO_SUBTASKS));
    noSubTasksLabel.addStyleName(Reindeer.LABEL_SMALL);
    subTasksLayout.addComponent(noSubTasksLabel);
  }
  
  protected void initRelatedContent() {
    relatedContentLayout = new VerticalLayout();
    relatedContentLayout.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addComponent(relatedContentLayout);
    initRelatedContentTitle();
    
    List<Attachment> attachments = taskService.getTaskAttachments(historicTask.getId());
    if (attachments.size() > 0) {
      Table table = initRelatedContentTable();
      populateRelatedContent(table, attachments);
    } else {
      initNoRelatedContentLabel();
    }
  }

  protected void initRelatedContentTitle() {
    Label title = new Label(ExplorerApp.get().getI18nManager().getMessage(Messages.TASK_RELATED_CONTENT));
    title.addStyleName(ExplorerLayout.STYLE_H3);
    title.setSizeFull();
    relatedContentLayout.addComponent(title);
  }

  protected Table initRelatedContentTable() {
    Table table = new Table();
    table.setWidth(100, UNITS_PERCENTAGE);
    table.addStyleName(ExplorerLayout.STYLE_RELATED_CONTENT_LIST);
    
    // Invisible by default, only shown when attachments are present
    table.setVisible(false);
    table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);

    table.addContainerProperty("type", Embedded.class, null);
    table.setColumnWidth("type", 16);
    table.addContainerProperty("name", Component.class, null);
    
    relatedContentLayout.addComponent(table);
    return table;
  }

  protected void populateRelatedContent(Table table, List<Attachment> attachments) {
    for (Attachment attachment : attachments) {
      AttachmentRenderer renderer = attachmentRendererManager.getRenderer(attachment);
      Item attachmentItem = table.addItem(attachment.getId());
      
      // Simple renderer that just shows a popup window with the attachment
      RelatedContentComponent relatedContentComponent = new RelatedContentComponent() {
        public void showAttachmentDetail(Attachment attachment) {
          AttachmentDetailPopupWindow popup = new AttachmentDetailPopupWindow(attachment);
          ExplorerApp.get().getViewManager().showPopupWindow(popup);   
        }
      };
      
      attachmentItem.getItemProperty("name").setValue(renderer.getOverviewComponent(attachment, relatedContentComponent));
      attachmentItem.getItemProperty("type").setValue(new Embedded(null, renderer.getImage(attachment)));
    }
    table.setPageLength(table.size());
  }
  
  protected void initNoRelatedContentLabel() {
    Label noContentLabel = new Label(i18nManager.getMessage(Messages.TASK_NO_RELATED_CONTENT));
    noContentLabel.addStyleName(Reindeer.LABEL_SMALL);
    relatedContentLayout.addComponent(noContentLabel);
  }
  
  /**
   * Returns a numeric priority - low (1), medium (2) or high (3) - 
   * based on the given numeric priority value.
   */
  protected Integer convertPriority(int priority) {
    // TODO: define thresholds
    return 1;
  }
  
}
