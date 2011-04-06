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
import org.activiti.explorer.ui.event.GenericFormEvent;

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
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


/**
 * @author Frederik Heremans
 */
public class CreateAttachmentPopupWindow extends Window {

  private static final long serialVersionUID = 1L;
  
  protected String taskId;
  protected String processInstanceId;

  protected I18nManager i18nManager;
  protected TaskService taskService;

  protected GridLayout layout;
  protected AttachmentEditor currentEditor;
  protected Table attachmentTypes;
  protected Button okButton;
  
  public CreateAttachmentPopupWindow() {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    
    setCaption(i18nManager.getMessage(Messages.RELATED_CONTENT_ADD));
    setWidth(50, UNITS_PERCENTAGE);
    setHeight(50, UNITS_PERCENTAGE);
    center();

    layout = new GridLayout(2,2);
    layout.setSpacing(true);
    layout.setMargin(true);
    layout.setSizeFull();
    setContent(layout);
    
    initTable();
    initActions();
  }
  
  @Override
  public void attach() {
    super.attach();
    if(attachmentTypes.size() > 0) {
      attachmentTypes.select(attachmentTypes.firstItemId());      
    }
  }

  protected void initActions() {
    okButton = new Button(i18nManager.getMessage(Messages.RELATED_CONTENT_CREATE));
    layout.addComponent(okButton, 1, 1);
    okButton.setEnabled(false);
    
    okButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        saveAttachment();
      }
    });
    
    layout.setComponentAlignment(okButton, Alignment.BOTTOM_RIGHT);
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
    
    attachmentTypes.addContainerProperty("type", Embedded.class, null);
    attachmentTypes.setColumnWidth("type", 16);
    attachmentTypes.addContainerProperty("name", String.class, null);
    
    // Add all possible attachment types
    for(AttachmentRenderer renderer : AttachmentRenderers.getAttachmentRenderers()) {
      String name = renderer.getName(i18nManager);
      Embedded image = null;
      
      Resource resource = renderer.getImage(null);
      if(resource != null) {
        image = new Embedded(null, resource);
      }
      
      Item item = attachmentTypes.addItem(renderer.getType());
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
    
    layout.addComponent(attachmentTypes, 0, 0, 0, 1);
    layout.setColumnExpandRatio(1, 1.0f);
    layout.setRowExpandRatio(0, 1.0f);
  }
  
  protected void selectType(String typeName) {
    if(typeName == null) {
      setCurrentEditor(null);
    } else {
      AttachmentRenderer renderer = AttachmentRenderers.getRenderer(typeName);
      setCurrentEditor(renderer.getEditor(null, taskId, processInstanceId));
    }
  }
  
  
  protected void setCurrentEditor(AttachmentEditor editor) {
    this.currentEditor = editor;
    if(layout.getComponent(0, 1) != null) {
      removeComponent(layout.getComponent(1, 0));
    }
    if(currentEditor != null) {
      currentEditor.setSizeFull();
      layout.addComponent(currentEditor, 1, 0);
      okButton.setEnabled(true);
    } else {
      okButton.setEnabled(false);
    }
  }
  
  protected void saveAttachment() {
    try {
      // Creation and persistence of attachment is done in editor
      Attachment attachment = currentEditor.getAttachment();
      
      fireEvent(new GenericFormEvent(this, GenericFormEvent.FORM_SUBMITTED, attachment));
      
      // Finally, close window
      close();
    } catch(InvalidValueException ive) {
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
