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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.Attachment;

/**
 * @author Frederik Heremans
 */
public class AttachmentRenderers {


  private static final List<AttachmentRenderer> renderers = new ArrayList<AttachmentRenderer>();
  private static final List<AttachmentEditor> editors = new ArrayList<AttachmentEditor>();
  private static final Map<String, AttachmentEditor> editorMap = new HashMap<String, AttachmentEditor>();
  
  public static void addAttachmentRenderer(AttachmentRenderer renderer) {
    renderers.add(renderer);
  }
  
  public static void addAttachmentEditor(AttachmentEditor editor) {
    editors.add(editor);
    editorMap.put(editor.getName(), editor);
  }

  public static AttachmentRenderer getRenderer(Attachment attachment) {
    return getRenderer(attachment.getType());
  }

  public static AttachmentRenderer getRenderer(String type) {
    for(AttachmentRenderer renderer : renderers) {
      if(renderer.canRenderAttachment(type)) {
        return renderer;
      }
    }
    // TODO: Use default renderer
    throw new ActivitiException("No renderer found for attachment of type: " + type);
  }
  
  
  /**
   * Gets all attachment editors known. Returned in the order they 
   * were added.
   */
  public static List<AttachmentEditor> getAttachmentEditors() {
    return Collections.unmodifiableList(editors);
  }

  public static AttachmentEditor getEditor(String type) {
    AttachmentEditor editor =  editorMap.get(type);
    if(editor == null) {
      throw new ActivitiException("No editor defined with the given name: " + editor);
    }
    return editor;
  }
}
