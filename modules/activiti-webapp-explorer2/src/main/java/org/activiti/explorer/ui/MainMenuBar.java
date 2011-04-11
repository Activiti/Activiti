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

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Joram Barrez
 */
@SuppressWarnings("serial")
public class MainMenuBar extends MenuBar {
  
  private static final long serialVersionUID = 1L;
  
  protected ViewManager viewManager;
  protected I18nManager i18nManager;
  
  public MainMenuBar() {
    this.viewManager = ExplorerApp.get().getViewManager();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    init();
  }
  
  protected void init() {
    Button taskButton = createMenuBarButton(i18nManager.getMessage(Messages.MAIN_MENU_TASKS));
    taskButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        viewManager.showTaskInboxPage();
      }
    });
    
    Button flowButton = createMenuBarButton(i18nManager.getMessage(Messages.MAIN_MENU_FLOWS));
    flowButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        viewManager.showFlowPage();
      }
    });
    
    if (ExplorerApp.get().getLoggedInUser().isAdmin()) {
      Button managementButton = createMenuBarButton(i18nManager.getMessage(Messages.MAIN_MENU_MANAGEMENT));
      managementButton.addListener(new ClickListener() {
        public void buttonClick(ClickEvent event) {
          viewManager.showDatabasePage();
        }
      });
    }
    
    fillRemainingSpace();
  }
  
}
