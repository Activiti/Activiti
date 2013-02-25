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

package org.activiti.explorer.ui.content;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Attachment;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.PopupWindow;
import org.activiti.explorer.ui.event.SubmitEvent;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Frederik Heremans
 */
public class CreateAttachmentPopupWindow extends PopupWindow {

  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected String processInstanceId;

  protected I18nManager i18nManager;
  protected AttachmentRendererManager attachmentRendererManager;
  protected transient TaskService taskService;

  protected HorizontalLayout layout;
  protected GridLayout detailLayout;
  protected AttachmentEditorComponent currentEditor;
  protected Table attachmentTypes;
  protected Button okButton;
  
  public CreateAttachmentPopupWindow() {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.attachmentRendererManager = ExplorerApp.get().getAttachmentRendererManager();
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();

    setCaption(i18nManager.getMessage(Messages.RELATED_CONTENT_ADD));
    setWidth(700, UNITS_PIXELS);
    setHeight(430, UNITS_PIXELS);
    center();
    setModal(true);
    addStyleName(Reindeer.WINDOW_LIGHT);

    layout = new HorizontalLayout();
    layout.setSpacing(false);
    layout.setMargin(true);
    layout.setSizeFull();
    setContent(layout);

    initTable();

    detailLayout = new GridLayout(1,2);
    detailLayout.setSizeFull();
    detailLayout.setMargin(true);
    detailLayout.setSpacing(true);
    detailLayout.addStyleName(ExplorerLayout.STYLE_RELATED_CONTENT_CREATE_DETAIL);
    
    layout.addComponent(detailLayout);
    layout.setExpandRatio(detailLayout, 1.0f);
    
    detailLayout.setRowExpandRatio(0, 1.0f);
    detailLayout.setColumnExpandRatio(0, 1.0f);
    initActions();
  }

  @Override
  public void attach() {
    super.attach();
    if (attachmentTypes.size() > 0) {
      attachmentTypes.select(attachmentTypes.firstItemId());
    }
  }

  protected void initActions() {
    okButton = new Button(i18nManager.getMessage(Messages.RELATED_CONTENT_CREATE));
    detailLayout.addComponent(okButton, 0, 1);
    okButton.setEnabled(false);
    okButton.addListener(new ClickListener() {

      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        saveAttachment();
      }
    });
    detailLayout.setComponentAlignment(okButton, Alignment.BOTTOM_RIGHT);
  }

  protected void initTable() {
    attachmentTypes = new Table();
    attachmentTypes.setSizeUndefined();
    attachmentTypes.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    attachmentTypes.setSelectable(true);
    attachmentTypes.setImmediate(true);
    attachmentTypes.setNullSelectionAllowed(false);
    attachmentTypes.setWidth(200, UNITS_PIXELS);
    attachmentTypes.setHeight(100, UNITS_PERCENTAGE);

    attachmentTypes.setCellStyleGenerator(new CellStyleGenerator() {
      private static final long serialVersionUID = 1L;
      public String getStyle(Object itemId, Object propertyId) {
        if("name".equals(propertyId)) {
          return ExplorerLayout.STYLE_RELATED_CONTENT_CREATE_LIST_LAST_COLUMN;
        }
        return null;
      }
    });

    attachmentTypes.addStyleName(ExplorerLayout.STYLE_RELATED_CONTENT_CREATE_LIST);

    attachmentTypes.addContainerProperty("type", Embedded.class, null);
    attachmentTypes.setColumnWidth("type", 16);
    attachmentTypes.addContainerProperty("name", String.class, null);

    // Add all possible attachment types
    for (AttachmentEditor editor : attachmentRendererManager.getAttachmentEditors()) {
      String name = editor.getTitle(i18nManager);
      Embedded image = null;

      Resource resource = editor.getImage();
      if (resource != null) {
        image = new Embedded(null, resource);
      }
      Item item = attachmentTypes.addItem(editor.getName());
      item.getItemProperty("type").setValue(image);
      item.getItemProperty("name").setValue(name);
    }

    // Add listener to show editor component
    attachmentTypes.addListener(new ValueChangeListener() {

      private static final long serialVersionUID = 1L;

      public void valueChange(ValueChangeEvent event) {
        String type = (String) event.getProperty().getValue();
        selectType(type);
      }
    });

    layout.addComponent(attachmentTypes);
  }

  protected void selectType(String type) {
    if (type != null) {
      setCurrentEditor(attachmentRendererManager.getEditor(type));
    } else {
      setCurrentEditor(null);
    }
  }

  protected void setCurrentEditor(AttachmentEditor editor) {
    AttachmentEditorComponent component = editor.getEditor(null, taskId, processInstanceId);
    this.currentEditor = component;
    detailLayout.removeComponent(detailLayout.getComponent(0, 0));

    if (currentEditor != null) {
      currentEditor.setSizeFull();
      detailLayout.addComponent(currentEditor, 0, 0);
      okButton.setEnabled(true);
    } else {
      okButton.setEnabled(false);
    }
  }

  protected void saveAttachment() {
    try {
      // Creation and persistence of attachment is done in editor
      Attachment attachment = currentEditor.getAttachment();

      fireEvent(new SubmitEvent(this, SubmitEvent.SUBMITTED, attachment));

      // Finally, close window
      close();
    } catch (InvalidValueException ive) {
      // Validation error, Editor UI will handle this.
    }
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
}
