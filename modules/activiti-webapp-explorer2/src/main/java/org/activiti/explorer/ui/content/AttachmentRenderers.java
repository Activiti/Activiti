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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.Attachment;

/**
 * @author Frederik Heremans
 */
public class AttachmentRenderers {

  private static Map<String, AttachmentRenderer> renderers = new LinkedHashMap<String, AttachmentRenderer>();

  public static void addAttachmentRenderer(AttachmentRenderer renderer) {
    renderers.put(renderer.getType(), renderer);
  }

  public static AttachmentRenderer getRenderer(Attachment attachment) {
    return getRenderer(attachment.getType());
  }

  public static AttachmentRenderer getRenderer(String type) {
    AttachmentRenderer renderer = renderers.get(type);
    if (renderer == null) {
      throw new ActivitiException("No renderer found for attachment of type: " + type);
    }
    return renderer;
  }
  
  /**
   * Gets all attachment renderers known. They are returned in the order they 
   * were added.
   */
  public static Collection<AttachmentRenderer> getAttachmentRenderers() {
    return renderers.values();
  }
}
