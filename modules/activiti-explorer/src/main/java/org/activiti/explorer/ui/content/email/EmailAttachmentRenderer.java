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

package org.activiti.explorer.ui.content.email;

import org.activiti.engine.task.Attachment;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.content.GenericAttachmentRenderer;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;


/**
 * @author Frederik Heremans
 */
public class EmailAttachmentRenderer extends GenericAttachmentRenderer {
  
  public static final String EMAIL_TYPE = "email";
  
  public boolean canRenderAttachment(String type) {
    return EMAIL_TYPE.equals(type);
  }

  public String getName(I18nManager i18nManager) {
    return i18nManager.getMessage(Messages.RELATED_CONTENT_TYPE_EMAIL);
  }

  public Resource getImage(Attachment attachment) {
    return Images.IMAP;
  }

 
  public Component getDetailComponent(Attachment attachment) {
    return new EmailDetailPanel(attachment);
  }

}
