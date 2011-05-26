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

import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;


/**
 * Entry in a tool bar. Shows pop-up with different items when clicked.
 * 
 * @author Frederik Heremans
 */
public class ToolbarPopupEntry extends ToolbarEntry {

  private static final long serialVersionUID = 1L;
  
  protected MenuBar menuBar;
  protected MenuItem rootItem;
  
  public ToolbarPopupEntry(String key, String title) {
    super(key, title);
  }
  
  /**
   * Add menu-item.
   */
  public MenuItem addMenuItem(String title) {
    return rootItem.addItem(title, null);
  }
  
  /**
   * Add a menu-item, which executes the given command when clicked.
   */
  public MenuItem addMenuItem(String title, final ToolbarCommand command) {
    return rootItem.addItem(title, new Command() {
      private static final long serialVersionUID = 1L;
      public void menuSelected(MenuItem selectedItem) {
        if(command != null) {
          command.toolBarItemSelected();
        }
      }
    });
  }
  
  @Override
  public void setActive(boolean active) {
    if(this.active != active) {
      this.active = active;
      if(active) {
        menuBar.addStyleName(ExplorerLayout.STYLE_ACTIVE);
        countButton.addStyleName(ExplorerLayout.STYLE_ACTIVE);
      } else {
        menuBar.removeStyleName(ExplorerLayout.STYLE_ACTIVE);
        countButton.removeStyleName(ExplorerLayout.STYLE_ACTIVE);
      }
    }
  }
  
  protected void initLabelComponent() {
    menuBar = new MenuBar();
    menuBar.addStyleName(ExplorerLayout.STYLE_TOOLBAR_POPUP);
    rootItem = menuBar.addItem(title, null);
    layout.addComponent(menuBar);
  }
}
