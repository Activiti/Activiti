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

import com.vaadin.terminal.Resource;


public interface AttachmentEditor {
  
  /**
   * Get the name of this editor. Only one editor with the same name can e used.
   * The last one wit the same name added to {@link AttachmentRendererManager} will
   * be used.
   */
  String getName();
  
  /**
   * Gets the human-readable name for the type of related content
   * this class is capable of rendering.
   */
  String getTitle(I18nManager i18nManager);
    
  /**
   * Get the image to display in the list of possible editors.
   */
  Resource getImage();
  
  /**
   * Get the component to display to edit the given attachment.
   * 
   * @param attachment the attachment to edit. Null if the editor should
   * create a new attachment when submitted.
   */
  AttachmentEditorComponent getEditor(Attachment attachment, String taskId, String processInstanceId);

}
