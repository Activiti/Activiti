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

import java.io.InputStream;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Attachment;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.content.GenericAttachmentRenderer;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.util.InputStreamStreamSource;
import org.activiti.explorer.util.ImageUtil;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;


/**
 * @author Frederik Heremans
 */
public class ImageAttachmentRenderer extends GenericAttachmentRenderer {

  @Override
  public boolean canRenderAttachment(String type) {
    return type != null && type.startsWith("image/");
  }
  
  @Override
  public Resource getImage(Attachment attachment) {
    return Images.RELATED_CONTENT_PICTURE;
  }
  
  @Override
  public Component getDetailComponent(Attachment attachment) {
    VerticalLayout verticalLayout = new VerticalLayout();
    verticalLayout.setSizeUndefined();
    verticalLayout.setSpacing(true);
    verticalLayout.setMargin(true);
    
    Label description = new Label(attachment.getDescription());
    description.setSizeUndefined();
    verticalLayout.addComponent(description);
    
    // Image
    TaskService taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    
    String mimeType = extractMineType(attachment.getType());
    
    InputStream imageStream = ImageUtil.smallify(taskService.getAttachmentContent(attachment.getId()), mimeType, 900, 550);
    Resource resource = new StreamResource(new InputStreamStreamSource(imageStream),
            attachment.getName() + extractExtention(attachment.getType()),ExplorerApp.get());
    Embedded image = new Embedded(null, resource);
    verticalLayout.addComponent(image);
    
    // Linke
    HorizontalLayout LinkLayout = new HorizontalLayout();
    LinkLayout.setSpacing(true);
    verticalLayout.addComponent(LinkLayout);
    verticalLayout.setComponentAlignment(LinkLayout, Alignment.MIDDLE_CENTER);
    
    Label fullSizeLabel = new Label(ExplorerApp.get().getI18nManager().getMessage(Messages.RELATED_CONTENT_SHOW_FULL_SIZE));
    LinkLayout.addComponent(fullSizeLabel);
    
    Link link = null;
    if(attachment.getUrl() != null) {
      link = new Link(attachment.getUrl(), new ExternalResource(attachment.getUrl()));
    } else {
      taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
      Resource res = new StreamResource(new InputStreamStreamSource(taskService.getAttachmentContent(attachment.getId())),
              attachment.getName() + extractExtention(attachment.getType()),ExplorerApp.get());
      
      link = new Link(attachment.getName(), res);
    }
    
    link.setIcon(Images.RELATED_CONTENT_PICTURE);
    link.setTargetName(ExplorerLayout.LINK_TARGET_BLANK);      
    LinkLayout.addComponent(link);
    
    return verticalLayout;
  }

  protected String extractMineType(String type) {
    if(type != null) {
      int index = type.lastIndexOf(FileAttachmentEditorComponent.MIME_TYPE_EXTENTION_SPLIT_CHAR);
      if(index >= 0){
        return type.substring(0, index);
      }
    }
    return type;
  }
  
}
