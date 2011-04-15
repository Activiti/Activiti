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
import org.activiti.explorer.ExplorerApp;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Window;


/**
 * @author Frederik Heremans
 */
public class AttachmentDetailPopupWindow extends Window {

  private static final long serialVersionUID = 1L;
  
  public AttachmentDetailPopupWindow(Attachment attachment) {
    super(attachment.getName());
    
    setWidth(50, UNITS_PERCENTAGE);
    setHeight(50, UNITS_PERCENTAGE);
    center();
    
    AttachmentRenderer renderer = ExplorerApp.get().getAttachmentRendererManager().getRenderer(attachment.getType());
    Component detail = renderer.getDetailComponent(attachment);
    
    if(detail instanceof ComponentContainer) {
      setContent((ComponentContainer) detail);
    } else {
      addComponent(detail);
    }
  }
}
