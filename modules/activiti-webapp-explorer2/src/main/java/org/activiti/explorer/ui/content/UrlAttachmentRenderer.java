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

import org.activiti.engine.task.Attachment;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.task.TaskRelatedContentComponent;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Frederik Heremans
 */
public class UrlAttachmentRenderer implements AttachmentRenderer {

  public static final String ATTACHMENT_TYPE = "url";
  
  public String getType() {
    return ATTACHMENT_TYPE;
  }

  public String getName(I18nManager i18nManager) {
    return i18nManager.getMessage(Messages.RELATED_CONTENT_TYPE_URL);
  }

  public Resource getImage(Attachment attachment) {
    // Always return the same image for every attachment
    return Images.RELATED_CONTENT_URL;
  }

  public Component getOverviewLink(final Attachment attachment, final TaskRelatedContentComponent parent) {
    Button attachmentLink = new Button(attachment.getName());
    attachmentLink.addStyleName(Reindeer.BUTTON_LINK);
    
    attachmentLink.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        // TODO: request detail from parent component
      }
    });
    return attachmentLink;
  }

  public AttachmentEditor getEditor(Attachment attachment, String taskId, String processInstanceId) {
    return new UrlAttachmentEditor(taskId, processInstanceId);
  }

  public Component getDetailComponent(Attachment attachment) {
    // TODO: make nice
    return new Label(attachment.getUrl());
  }

}
