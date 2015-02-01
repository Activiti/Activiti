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

import org.activiti.engine.task.Attachment;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.content.AttachmentRenderer;
import org.activiti.explorer.ui.content.RelatedContentComponent;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
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
public class UrlAttachmentRenderer implements AttachmentRenderer {

  public static final String ATTACHMENT_TYPE = "url";
  
  public String getName(I18nManager i18nManager) {
    return i18nManager.getMessage(Messages.RELATED_CONTENT_TYPE_URL);
  }

  public Resource getImage(Attachment attachment) {
    // Always return the same image for every attachment
    return Images.RELATED_CONTENT_URL;
  }

  public Component getOverviewComponent(final Attachment attachment, final RelatedContentComponent parent) {
    
    // If the attachment has no description, overview link is link to actual page
    // instead of showing popup with details.
    if(attachment.getDescription() != null && !"".equals(attachment.getDescription())) {
      Button attachmentLink = new Button(attachment.getName());
      attachmentLink.addStyleName(Reindeer.BUTTON_LINK);
      
      attachmentLink.addListener(new ClickListener() {
        private static final long serialVersionUID = 1L;
        
        public void buttonClick(ClickEvent event) {
          parent.showAttachmentDetail(attachment);
        }
      });
      return attachmentLink;
    } else {
      return new Link(attachment.getName(), new ExternalResource(attachment.getUrl()));
    }
  }

  public Component getDetailComponent(Attachment attachment) {
    VerticalLayout verticalLayout = new VerticalLayout();
    verticalLayout.setSpacing(true);
    verticalLayout.setMargin(true);
    
    verticalLayout.addComponent(new Label(attachment.getDescription()));
    
    HorizontalLayout linkLayout = new HorizontalLayout();
    linkLayout.setSpacing(true);
    verticalLayout.addComponent(linkLayout);
    
    // Icon
    linkLayout.addComponent(new Embedded(null, Images.RELATED_CONTENT_URL));
    
    // Link
    Link link = new Link(attachment.getUrl(), new ExternalResource(attachment.getUrl()));
    link.setTargetName(ExplorerLayout.LINK_TARGET_BLANK);
    linkLayout.addComponent(link);
    
    return verticalLayout;
  }

  public boolean canRenderAttachment(String type) {
    return ATTACHMENT_TYPE.equals(type);
  }

}
