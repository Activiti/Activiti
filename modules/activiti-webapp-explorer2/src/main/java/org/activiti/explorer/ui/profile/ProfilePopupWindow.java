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

package org.activiti.explorer.ui.profile;

import org.activiti.explorer.ui.custom.PopupWindow;

import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class ProfilePopupWindow extends PopupWindow {

  private static final long serialVersionUID = 3129077881658239761L;
  
  public ProfilePopupWindow(String userId) {
    addStyleName(Reindeer.WINDOW_LIGHT);
    setModal(true);
    setHeight("80%");
    setWidth("50%");
    center();
    addComponent(new ProfilePanel(userId));
  }
  

}
