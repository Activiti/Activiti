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

package org.activiti.explorer.ui.content.url;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Attachment;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.content.AttachmentEditorComponent;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Form;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;


/**
 * @author Frederik Heremans
 */
public class UrlAttachmentEditorComponent extends Form implements AttachmentEditorComponent {

  private static final long serialVersionUID = 1L;
  
  protected Attachment attachment;
  protected String taskId;
  protected String processInstanceId;
  
  protected I18nManager i18nManager;
  protected transient TaskService taskService;
  
  public UrlAttachmentEditorComponent(String taskId, String processInstanceId) {
    this(null, taskId, processInstanceId);
  }
  
  public UrlAttachmentEditorComponent(Attachment attachment, String taskId, String processInstanceId) {
    this.attachment = attachment;
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    
    this.i18nManager = ExplorerApp.get().getI18nManager();
    taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    
    setSizeFull();
    setDescription(i18nManager.getMessage(Messages.RELATED_CONTENT_TYPE_URL_HELP));
    
    initUrl();
    initName();
    initDescription();
  }

  protected void initUrl() {
    TextField urlField = new TextField(i18nManager.getMessage(Messages.RELATED_CONTENT_TYPE_URL_URL));
    urlField.focus();
    urlField.setRequired(true);
    urlField.setRequiredError(i18nManager.getMessage(Messages.RELATED_CONTENT_TYPE_URL_URL_REQUIRED));
    urlField.setWidth(100, UNITS_PERCENTAGE);
    // URL isn't mutable once attachment is created
    if(attachment != null) {
      urlField.setEnabled(false);
    }
    
    addField("url", urlField);
  }

  protected void initDescription() {
    TextArea descriptionField = new TextArea(i18nManager.getMessage(Messages.RELATED_CONTENT_DESCRIPTION));
    descriptionField.setWidth(100, UNITS_PERCENTAGE);
    descriptionField.setHeight(100, UNITS_PIXELS);
    addField("description", descriptionField);
  }

  protected void initName() {
    TextField nameField = new TextField(i18nManager.getMessage(Messages.RELATED_CONTENT_NAME));
    nameField.setWidth(100, UNITS_PERCENTAGE);
    addField("name", nameField);
  }

  public Attachment getAttachment() throws InvalidValueException {
    // Force validation of the fields
    commit();
    if(attachment != null) {
      applyValuesToAttachment();
    } else {
      // Create new attachment based on values
      // TODO: use explorerApp to get service
      attachment = taskService.createAttachment(UrlAttachmentRenderer.ATTACHMENT_TYPE, taskId, processInstanceId, 
          getAttachmentName(), getAttachmentDescription(), getAttachmentUrl());
    }
    return attachment;
  }
  
  protected String getAttachmentUrl() {
    return (String) getFieldValue("url");
  }
  
  protected String getAttachmentName() {
    String name = (String) getFieldValue("name");
    if(name == null) {
      name = getAttachmentUrl();
    }
    return name;
  }
  
  protected String getAttachmentDescription() {
    return getFieldValue("description");
  }
  
  protected String getFieldValue(String key) {
    String value = (String) getField(key).getValue();
    if("".equals(value)) {
      return null;
    }
    return value;
  }
  
  private void applyValuesToAttachment() {
    attachment.setName(getAttachmentName());
    attachment.setDescription(getAttachmentDescription());
  }
}
