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
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.content.file.FileAttachmentEditorComponent;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.util.InputStreamStreamSource;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Frederik Heremans
 */
public class GenericAttachmentRenderer implements AttachmentRenderer {

  public boolean canRenderAttachment(String type) {
    // Render everything
    return true;
  }

  public String getName(I18nManager i18nManager) {
    return i18nManager.getMessage(Messages.RELATED_CONTENT_TYPE_FILE);
  }

  public Resource getImage(Attachment attachment) {
    return Images.RELATED_CONTENT_FILE;
  }

  public Component getOverviewComponent(final Attachment attachment, final RelatedContentComponent parent) {
    Button attachmentLink = new Button(attachment.getName());
    attachmentLink.addStyleName(Reindeer.BUTTON_LINK);
    
    attachmentLink.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        parent.showAttachmentDetail(attachment);
      }
    });
    return attachmentLink;
  }

  public Component getDetailComponent(Attachment attachment) {
    VerticalLayout verticalLayout = new VerticalLayout();
    verticalLayout.setSizeUndefined();
    verticalLayout.setSpacing(true);
    verticalLayout.setMargin(true);
    
    Label description = new Label(attachment.getDescription());
    description.setSizeUndefined();
    verticalLayout.addComponent(description);
    
    HorizontalLayout linkLayout = new HorizontalLayout();
    linkLayout.setSpacing(true);
    verticalLayout.addComponent(linkLayout);
    
    // Image
    linkLayout.addComponent(new Embedded(null, getImage(attachment)));
    
    // Link
    Link link = null;
    if(attachment.getUrl() != null) {
      link = new Link(attachment.getUrl(), new ExternalResource(attachment.getUrl()));
    } else {
      TaskService taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
      Resource res = new StreamResource(new InputStreamStreamSource(taskService.getAttachmentContent(attachment.getId())),
              attachment.getName() + extractExtention(attachment.getType()),ExplorerApp.get());
      
      link = new Link(attachment.getName(), res);
    }
    
    // Set generic image and external window 
    link.setTargetName(ExplorerLayout.LINK_TARGET_BLANK);      
    linkLayout.addComponent(link);
    
    return verticalLayout;
  }

  protected String extractExtention(String type) {
    // Check if the extention is appended at the end
    int lastIndex = type.lastIndexOf(FileAttachmentEditorComponent.MIME_TYPE_EXTENTION_SPLIT_CHAR);
    if(lastIndex > 0 && lastIndex < type.length() - 1) {
      return "." + type.substring(lastIndex + 1);
    }
    
    // No extention added to end of mime-type, used second part of mime-type (eg. image/png -> .png)
    lastIndex = type.lastIndexOf('/');
    if(lastIndex > 0 && lastIndex < type.length() - 1) {
      return "." + type.substring(lastIndex + 1);
    }
    return "." + type;
  }

}
