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

import org.activiti.engine.task.Attachment;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.content.AttachmentEditor;
import org.activiti.explorer.ui.content.AttachmentEditorComponent;

import com.vaadin.terminal.Resource;


/**
 * @author Frederik Heremans
 */
public class FileAttachmentEditor implements AttachmentEditor {

  public static final String FILE_ATTACHMENT_TYPE = "file";
  
  public String getName() {
    return FILE_ATTACHMENT_TYPE;
  }

  public String getTitle(I18nManager i18nManager) {
    return i18nManager.getMessage(Messages.RELATED_CONTENT_TYPE_FILE);
  }

  public Resource getImage() {
    return Images.RELATED_CONTENT_FILE;
  }

  public AttachmentEditorComponent getEditor(Attachment attachment, String taskId, String processInstanceId) {
    return new FileAttachmentEditorComponent(attachment, taskId, processInstanceId);
  }

}
