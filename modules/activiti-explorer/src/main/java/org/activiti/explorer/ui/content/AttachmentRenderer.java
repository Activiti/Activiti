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

import org.activiti.engine.task.Attachment;
import org.activiti.explorer.I18nManager;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;


/**
 * Representing a class that is capable of rendering related content ({@link Attachment}s)
 * for a certain type in different contexts: overview link, edit/add and detail.
 * 
 * @author Frederik Heremans
 */
public interface AttachmentRenderer extends Serializable {

  /**
   * Returns true if this renderer is capable of rendering attachments of the given type.
   */
  boolean canRenderAttachment(String type);
  
  /**
   * Gets the human-readable name for the type of related content
   * this class is capable of rendering.
   */
  String getName(I18nManager i18nManager);
    
  /**
   * Get the image to display in the list of related content for
   * the given attachment.
   * 
   * @param attachment the attachment to get the image for. When null is
   * passed, the image for a new attachment (of the type this class is capable of
   * rendering) is expected to be returned.
   */
  Resource getImage(Attachment attachment);
  
  /**
   * Get the component that is used in the related content overview list.
   * Use the passed parent for calling back to show detail.
   */
  Component getOverviewComponent(Attachment attachment, RelatedContentComponent parent);
  
  /**
   * Get the component to render when viewing the details of the
   * related content for the type this renderer is responsible for.
   */
  Component getDetailComponent(Attachment attachment);
}
