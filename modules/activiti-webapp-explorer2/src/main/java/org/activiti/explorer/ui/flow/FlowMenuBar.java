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

package org.activiti.explorer.ui.flow;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;

import com.vaadin.ui.MenuBar;



/**
 * @author Joram Barrez
 */
public class FlowMenuBar extends MenuBar {
  
  private static final long serialVersionUID = 1L;

  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  
  public FlowMenuBar() {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    
    init();
  }
  
  protected void init() {
    setWidth("100%");

    addItem(i18nManager.getMessage(Messages.FLOW_MENU_MY_FLOWS), new Command() {
      
      private static final long serialVersionUID = 1801881272806784326L;

      public void menuSelected(MenuItem selectedItem) {
        viewManager.showMyFlowsPage();
      }
    });
    addItem(i18nManager.getMessage(Messages.FLOW_MENU_LAUNCH_FLOW), new Command() {
      
      private static final long serialVersionUID = -3389463332173619289L;

      public void menuSelected(MenuItem selectedItem) {
        viewManager.showFlowPage();
      }
    });
  }

}
