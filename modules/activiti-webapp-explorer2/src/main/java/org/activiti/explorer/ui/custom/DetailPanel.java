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

package org.activiti.explorer.ui.custom;

import org.activiti.explorer.ui.ExplorerLayout;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;


/**
 * Panel that should be used for main-content.
 * 
 * @author Frederik Heremans
 */
public class DetailPanel extends CustomLayout {

  private static final long serialVersionUID = 1L;
  
  public DetailPanel() {
    super(ExplorerLayout.CUSTOM_LAYOUT_CONTENT_DETAIL);
    setSizeFull();
  }
  
  /**
   * Set the actual content of the panel.
   */
  public void setDetailContent(Component component) {
    addComponent(component, "content");
  }
  
  /**
   * Set the component that is rendered in a fixed position
   * below the content. When content is scrolled, this component
   * stays visible all the time.
   */
  public void setFixedButtons(Component component) {
    addComponent(component, "buttons-bottom");
  }
  
}
