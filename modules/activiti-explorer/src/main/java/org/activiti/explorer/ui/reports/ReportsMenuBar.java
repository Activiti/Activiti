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
package org.activiti.explorer.ui.reports;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.custom.ToolbarEntry.ToolbarCommand;

/**
 * @author Joram Barrez
 */
public class ReportsMenuBar extends ToolBar {

  private static final long serialVersionUID = 1L;
  
  public static final String ENTRY_RUN_REPORTS = "runReports"; 
  public static final String ENTRY_SAVED_REPORTS = "savedResults"; 
  
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  
  public ReportsMenuBar() {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    setWidth("100%");
    
    initToolbarEntries();
  }
  
  protected void initToolbarEntries() {
    addRunReportsToolbarEntry();
    addSavedReportsToolbarEntry();
  }

  protected void addRunReportsToolbarEntry() {
    addToolbarEntry(ENTRY_RUN_REPORTS, i18nManager.getMessage(Messages.REPORTING_MENU_RUN_REPORTS), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showRunReportPage();
      }
    });
  }
  
  protected void addSavedReportsToolbarEntry() {
    addToolbarEntry(ENTRY_SAVED_REPORTS, i18nManager.getMessage(Messages.REPORTING_MENU_SAVED_REPORTS), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showSavedReportPage();
      }
    });
  }

}
