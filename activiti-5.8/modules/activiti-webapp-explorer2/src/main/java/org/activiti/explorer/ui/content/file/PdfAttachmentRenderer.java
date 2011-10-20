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
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.content.GenericAttachmentRenderer;

import com.vaadin.terminal.Resource;


/**
 * @author Frederik Heremans
 */
public class PdfAttachmentRenderer extends GenericAttachmentRenderer {
  
  private static final String PDF_ATTACHMENT_TYPE = "application/pdf";
  
  @Override
  public boolean canRenderAttachment(String type) {
    if(type != null) {
      return type.startsWith(PDF_ATTACHMENT_TYPE);
    }
    return false;
  }
  
  @Override
  public Resource getImage(Attachment attachment) {
    return Images.RELATED_CONTENT_PDF;
  }
}
