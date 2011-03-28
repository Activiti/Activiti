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

package org.activiti.explorer.ui;


import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class MenuBar extends HorizontalLayout {
  
  private static final long serialVersionUID = 5643382350306433838L;
  
  public MenuBar() {
    setSpacing(true);
    setWidth("100%");
  }
  
  protected Button createMenuBarButton(String name) {
    Button button = new Button(name);
    button.addStyleName(Reindeer.BUTTON_LINK);
    button.addStyleName(ExplorerLayout.STYLE_MENUBAR_BUTTON);
    addComponent(button);
    return button;
  }
  
  protected void fillRemainingSpace() {
    Label remainingSpace = new Label();
    remainingSpace.setWidth("100%");
    addComponent(remainingSpace);
    setExpandRatio(remainingSpace, 1.0f);
  }

}
