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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.content.AttachmentDetailPopupWindow;
import org.activiti.explorer.ui.content.AttachmentRenderer;
import org.activiti.explorer.ui.content.AttachmentRendererManager;
import org.activiti.explorer.ui.content.CreateAttachmentPopupWindow;
import org.activiti.explorer.ui.content.RelatedContentComponent;
import org.activiti.explorer.ui.custom.ConfirmationDialogPopupWindow;
import org.activiti.explorer.ui.event.ConfirmationEvent;
import org.activiti.explorer.ui.event.ConfirmationEventListener;
import org.activiti.explorer.ui.event.SubmitEvent;
import org.activiti.explorer.ui.event.SubmitEventListener;

import com.vaadin.data.Item;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Component for showing related content of a task. Also allows adding, removing
 * and opening related content.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class TaskRelatedContentComponent extends VerticalLayout implements RelatedContentComponent {

  private static final long serialVersionUID = 1L;
  
  protected TaskService taskService;
  protected AttachmentRendererManager attachmentRendererManager;
  
  protected Task task;
  protected Table table;
  protected TaskDetailPanel taskDetailPanel;

  public TaskRelatedContentComponent(Task task, TaskDetailPanel taskDetailPanel) {
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.attachmentRendererManager = ExplorerApp.get().getAttachmentRendererManager();
    
    this.task = task;
    this.taskDetailPanel = taskDetailPanel;
    
    initActions();
    initAttachmentTable();
  }
  
  public void showAttachmentDetail(Attachment attachment) {
    // Show popup window with detail of attachment rendered in in
    AttachmentDetailPopupWindow popup = new AttachmentDetailPopupWindow(attachment);
    ExplorerApp.get().getViewManager().showPopupWindow(popup);    
  }
 
  protected void initActions() {
    HorizontalLayout actionsContainer = new HorizontalLayout();
    actionsContainer.setSizeFull();

    // Title
    Label processTitle = new Label(ExplorerApp.get().getI18nManager().getMessage(Messages.TASK_RELATED_CONTENT));
    processTitle.addStyleName(ExplorerLayout.STYLE_RELATED_CONTENT_DETAILS_HEADER);
    processTitle.setSizeFull();
    actionsContainer.addComponent(processTitle);
    actionsContainer.setComponentAlignment(processTitle, Alignment.MIDDLE_LEFT);
    actionsContainer.setExpandRatio(processTitle, 1.0f);

    // Add content button
    Embedded addRelatedContentButton = new Embedded(null, Images.ADD);
    addRelatedContentButton.addStyleName(ExplorerLayout.STYLE_IMAGE_ACTION);
    addRelatedContentButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      public void click(ClickEvent event) {
        CreateAttachmentPopupWindow popup = new CreateAttachmentPopupWindow();
        popup.setTaskId(task.getId());
        
        // Add listener to update attachments when added
        popup.addListener(new SubmitEventListener() {
          
          private static final long serialVersionUID = 1L;

          @Override
          protected void submitted(SubmitEvent event) {
            taskDetailPanel.notifyRelatedContentChanged();
          }
          
          @Override
          protected void cancelled(SubmitEvent event) {
            // No attachment was added so updating UI isn't needed.
          }
        });
        
        ExplorerApp.get().getViewManager().showPopupWindow(popup);
      }
    });
    
    actionsContainer.addComponent(addRelatedContentButton);
    actionsContainer.setComponentAlignment(processTitle, Alignment.MIDDLE_RIGHT);
    
    
    addComponent(actionsContainer);
  }

  protected void initAttachmentTable() {
    table = new Table();
    table.setWidth(100, UNITS_PERCENTAGE);
    
    // Invisible by default, only shown when attachments are present
    table.setVisible(false);
    table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);

    addContainerProperties();

    // Get the related content for this task
    refreshTaskAttachments();

    addComponent(table);
  }

  protected void addContainerProperties() {
    table.addContainerProperty("type", Embedded.class, null);
    table.setColumnWidth("type", 16);
    
    table.addContainerProperty("name", Component.class, null);
    
    table.addContainerProperty("delete", Embedded.class, null);
    table.setColumnWidth("delete", 16);
  }
  
  public void refreshTaskAttachments() {
    if(table.size() > 0) {
      table.removeAllItems();
    }

    List<Attachment> attachments = taskService.getTaskAttachments(task.getId());
    addAttachmentsToTable(attachments);
  }

  protected void addAttachmentsToTable(List<Attachment> attachments) {
    
    for (Attachment attachment : attachments) {
      AttachmentRenderer renderer = attachmentRendererManager.getRenderer(attachment);
      Item attachmentItem = table.addItem(attachment.getId());
      attachmentItem.getItemProperty("name").setValue(renderer.getOverviewComponent(attachment, this));
      attachmentItem.getItemProperty("type").setValue(new Embedded(null, renderer.getImage(attachment)));
      
      Embedded deleteButton = new Embedded(null, Images.DELETE);
      deleteButton.addListener((ClickListener) new DeleteClickedListener(attachment));
      attachmentItem.getItemProperty("delete").setValue(deleteButton);
    }
    
    if(table.getItemIds().size() > 0) {
      table.setVisible(true);
    }
  }
  
  protected void addEmptySpace(ComponentContainer container) {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    container.addComponent(emptySpace);
  }
  
  private class DeleteClickedListener extends ConfirmationEventListener implements ClickListener {

    private static final long serialVersionUID = 1L;
    private Attachment attachment;
    
    public DeleteClickedListener(Attachment attachment) {
      this.attachment = attachment;
    }

    public void click(ClickEvent event) {
      I18nManager i18nManager = ExplorerApp.get().getI18nManager();
      
      ConfirmationDialogPopupWindow confirm = new ConfirmationDialogPopupWindow(
        i18nManager.getMessage(Messages.RELATED_CONTENT_CONFIRM_DELETE, attachment.getName()));
      
      confirm.addListener((ConfirmationEventListener)this);
      confirm.showConfirmation();
    }

    @Override
    protected void confirmed(ConfirmationEvent event) {
      taskService.deleteAttachment(attachment.getId());
      taskDetailPanel.notifyRelatedContentChanged();
    }

    @Override
    protected void rejected(ConfirmationEvent event) {
      // Nothing to do here
    }
  }


}
