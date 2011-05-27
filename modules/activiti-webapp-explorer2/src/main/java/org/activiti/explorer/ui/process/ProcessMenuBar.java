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

package org.activiti.explorer.ui.process;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.custom.ToolbarEntry.ToolbarCommand;



/**
 * @author Joram Barrez
 */
public class ProcessMenuBar extends ToolBar {
  
  private static final long serialVersionUID = 1L;
  
  public static final String ENTRY_MY_PROCESS_INSTANCES = "myProcessInstances"; 
  public static final String PROCESS_DEFINITIONS = "processDefinitions"; 

  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  
  public ProcessMenuBar() {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    
    init();
  }
  
  protected void init() {
    setWidth("100%");

    addToolbarEntry(ENTRY_MY_PROCESS_INSTANCES, i18nManager.getMessage(Messages.PROCESS_MENU_MY_INSTANCES), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showMyProcessInstancesPage();
      }
    });
    
    addToolbarEntry(PROCESS_DEFINITIONS, i18nManager.getMessage(Messages.PROCESS_MENU_DEFINITIONS), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showProcessDefinitionPage();
      }
    });
  }
}
