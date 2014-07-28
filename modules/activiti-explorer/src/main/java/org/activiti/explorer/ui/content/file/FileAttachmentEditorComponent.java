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

package org.activiti.explorer.ui.content.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Attachment;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.content.AttachmentEditorComponent;
import org.activiti.explorer.ui.custom.UploadComponent;
import org.activiti.explorer.ui.custom.UploadComponentFactory;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.VerticalLayout;


/**
 * @author Frederik Heremans
 */
public class FileAttachmentEditorComponent extends VerticalLayout implements AttachmentEditorComponent {

  private static final long serialVersionUID = 1L;

  public static final String MIME_TYPE_EXTENTION_SPLIT_CHAR = ";";
  
  protected Attachment attachment;
  protected String taskId;
  protected String processInstanceId;
  
  // File related
  protected String fileName;
  protected ByteArrayOutputStream byteArrayOutputStream;
  protected String mimeType;
  protected boolean fileUploaded = false;
  
  protected I18nManager i18nManager;
  protected transient TaskService taskService;
  
  protected Form form;
  protected UploadComponent uploadComponent;
  protected Label successIndicator;
  
  public FileAttachmentEditorComponent(String taskId, String processInstanceId) {
    this(null, taskId, processInstanceId);
  }
  
  public FileAttachmentEditorComponent(Attachment attachment, String taskId, String processInstanceId) {
    this.attachment = attachment;
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    
    this.i18nManager = ExplorerApp.get().getI18nManager();
    taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    
    form = new Form();
    form.setDescription(i18nManager.getMessage(Messages.RELATED_CONTENT_TYPE_FILE_HELP));
    setSizeFull();
    addComponent(form);
    initSuccessIndicator();
    initFileUpload();
    initName();
    initDescription();
  }

  protected void initSuccessIndicator() {
    successIndicator = new Label();
    successIndicator.setIcon(Images.SUCCESS);
    successIndicator.setVisible(false);
    
    addComponent(successIndicator);
  }

  protected void initFileUpload() {
    uploadComponent = ExplorerApp.get().getComponentFactory(UploadComponentFactory.class).create(); 
    
    Receiver receiver = new Receiver() {
      private static final long serialVersionUID = 1L;
      
      public OutputStream receiveUpload(String filename, String mType) {
        fileName = filename;
        
        // Try extracting the extention as well, and append it to the mime-type
        String extention = extractExtention(filename);
        if(extention != null) {
          mimeType = mType + MIME_TYPE_EXTENTION_SPLIT_CHAR + extention;
        } else {
          mimeType = mType;
        }
        
        // TODO: Refactor, don't use BAOS!!
        byteArrayOutputStream = new ByteArrayOutputStream();
        return byteArrayOutputStream;
      }
    };
    
    uploadComponent.setReceiver(receiver);
    uploadComponent.addFinishedListener(new FinishedListener() {
      
      private static final long serialVersionUID = 1L;

      public void uploadFinished(FinishedEvent event) {
        // Update UI
        if(getAttachmentName() == null || "".equals(getAttachmentName())) {
          setAttachmentName(getFriendlyName(fileName));
        }
        
        fileUploaded = true;
        successIndicator.setVisible(true);
        successIndicator.setCaption(i18nManager.getMessage(Messages.RELATED_CONTENT_TYPE_FILE_UPLOADED, fileName));
        form.setComponentError(null);
      }
    });
    
    addComponent(uploadComponent);
    setExpandRatio(uploadComponent, 1.0f);
  }
  
  protected String extractExtention(String fileName) {
    int lastIndex = fileName.lastIndexOf('.');
    if(lastIndex > 0 && lastIndex < fileName.length() - 1) {
      return fileName.substring(lastIndex + 1);
    }
    return null;
  }

  protected String getFriendlyName(String name) {
    if(name != null) {
      String friendlyName = null;
      int lastDotIndex = name.lastIndexOf(".");
      if(lastDotIndex > 0) {
        friendlyName = name.substring(0, name.length() - (name.length() - lastDotIndex));
      } else {
        friendlyName = name;
      }
      return friendlyName.replace("_", " ").replace("-", " ");
    }
    return name;
  }

  protected void initDescription() {
    TextArea descriptionField = new TextArea(i18nManager.getMessage(Messages.RELATED_CONTENT_DESCRIPTION));
    descriptionField.setWidth(100, UNITS_PERCENTAGE);
    descriptionField.setHeight(50, UNITS_PIXELS);
    form.addField("description", descriptionField);
  }

  protected void initName() {
    TextField nameField = new TextField(i18nManager.getMessage(Messages.RELATED_CONTENT_NAME));
    nameField.focus();
    nameField.setRequired(true);
    nameField.setRequiredError(i18nManager.getMessage(Messages.RELATED_CONTENT_NAME_REQUIRED));
    nameField.setWidth(100, UNITS_PERCENTAGE);
    form.addField("name", nameField);
  }

  public Attachment getAttachment() throws InvalidValueException {
    // Force validation of the fields
    form.commit();
    
    // Check if file is uploaded
    if(!fileUploaded) {
      InvalidValueException ive = new InvalidValueException(i18nManager.getMessage(Messages.RELATED_CONTENT_TYPE_FILE_REQUIRED));
      form.setComponentError(ive);
      throw ive;
    }
    
    if(attachment != null) {
      applyValuesToAttachment();
    } else {
      // Create new attachment based on values
      // TODO: use explorerApp to get services
      attachment = taskService.createAttachment(mimeType, taskId, processInstanceId, 
          getAttachmentName(), getAttachmentDescription(), new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    }
    return attachment;
  }
  
  protected void setAttachmentName(String name) {
    form.getField("name").setValue(name);
  }
  
  protected String getAttachmentName() {
    return (String) form.getField("name").getValue();
  }
  
  protected String getAttachmentDescription() {
    return (String) form.getField("description").getValue();
  }
  
  private void applyValuesToAttachment() {
    attachment.setName(getAttachmentName());
    attachment.setDescription(getAttachmentDescription());
  }
  
}
