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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.Attachment;
import org.activiti.explorer.ui.content.email.EmailAttachmentRenderer;
import org.activiti.explorer.ui.content.file.FileAttachmentEditor;
import org.activiti.explorer.ui.content.file.ImageAttachmentRenderer;
import org.activiti.explorer.ui.content.file.PdfAttachmentRenderer;
import org.activiti.explorer.ui.content.url.UrlAttachmentEditor;
import org.activiti.explorer.ui.content.url.UrlAttachmentRenderer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
@Component
public class AttachmentRendererManager implements InitializingBean, Serializable {

  private static final long serialVersionUID = 1L;
  private final List<AttachmentRenderer> renderers = new ArrayList<AttachmentRenderer>();
  private final List<AttachmentEditor> editors = new ArrayList<AttachmentEditor>();
  
  private final Map<String, AttachmentEditor> editorMap = new HashMap<String, AttachmentEditor>();
  private final AttachmentRenderer defaultAttachmentRenderer = new GenericAttachmentRenderer();
  
  public void addAttachmentRenderer(AttachmentRenderer renderer) {
    renderers.add(renderer);
  }
  
  public void addAttachmentEditor(AttachmentEditor editor) {
    editors.add(editor);
    editorMap.put(editor.getName(), editor);
  }

  public AttachmentRenderer getRenderer(Attachment attachment) {
    return getRenderer(attachment.getType());
  }

  public AttachmentRenderer getRenderer(String type) {
    for(AttachmentRenderer renderer : renderers) {
      if(renderer.canRenderAttachment(type)) {
        return renderer;
      }
    }
    return defaultAttachmentRenderer;
  }
  
  /**
   * Gets all attachment editors known. Returned in the order they 
   * were added.
   */
  public List<AttachmentEditor> getAttachmentEditors() {
    return Collections.unmodifiableList(editors);
  }

  public AttachmentEditor getEditor(String type) {
    AttachmentEditor editor =  editorMap.get(type);
    if(editor == null) {
      throw new ActivitiException("No editor defined with the given name: " + editor);
    }
    return editor;
  }
  
  public void afterPropertiesSet() throws Exception {
    // URL
    addAttachmentRenderer(new UrlAttachmentRenderer());
    addAttachmentEditor(new UrlAttachmentEditor());
    
    // Regular file upload
    addAttachmentEditor(new FileAttachmentEditor());
    
    // Basic types
    addAttachmentRenderer(new PdfAttachmentRenderer());
    addAttachmentRenderer(new ImageAttachmentRenderer());
    addAttachmentRenderer(new EmailAttachmentRenderer());
  }
}
