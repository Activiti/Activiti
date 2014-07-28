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
package org.activiti.explorer.ui.management;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.custom.ToolbarEntry.ToolbarCommand;
import org.activiti.explorer.ui.custom.ToolbarPopupEntry;
import org.activiti.explorer.ui.management.deployment.NewDeploymentListener;

/**
 * @author Joram Barrez
 */
public class ManagementMenuBar extends ToolBar {

  private static final long serialVersionUID = 1L;
  
  public static final String ENTRY_DATABASE = "database"; 
  public static final String ENTRY_DEPLOYMENTS = "deployments"; 
  public static final String ENTRY_ACTIVE_PROCESS_DEFINITIONS = "activeProcessDefinitions";
  public static final String ENTRY_SUSPENDED_PROCESS_DEFINITIONS = "suspendedProcessDefinitions";
  public static final String ENTRY_JOBS = "jobs"; 
  public static final String ENTRY_USERS = "users";
  public static final String ENTRY_GROUPS = "groups";
  public static final String ENTRY_ADMIN = "administration";
  public static final String ENTRY_CRYSTALBALL = "crystalball";
  
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  
  public ManagementMenuBar() {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    setWidth("100%");
    
    initToolbarEntries();
  }
  
  protected void initToolbarEntries() {
    addDatabaseToolbarEntry();
    addDeploymentsToolbarEntry();
    addActiveProcessDefinitionsEntry();
    addSuspendedProcessDefinitionsEntry();
    addJobsToolbarEntry();
    addUsersToolbarEntry();
    addGroupToolbarEntry();
    addAdministrationToolbarEntry();
    addCrystalBallToolbarEntry();
  }

  protected void addDatabaseToolbarEntry() {
    addToolbarEntry(ENTRY_DATABASE, i18nManager.getMessage(Messages.MGMT_MENU_DATABASE), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showDatabasePage();
      }
    });
  }

  protected void addDeploymentsToolbarEntry() {
    ToolbarPopupEntry deploymentEntry = addPopupEntry(ENTRY_DEPLOYMENTS, i18nManager.getMessage(Messages.MGMT_MENU_DEPLOYMENTS));
    deploymentEntry.addMenuItem(i18nManager.getMessage(Messages.MGMT_MENU_DEPLOYMENTS_SHOW_ALL), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showDeploymentPage();
      }
    });
    deploymentEntry.addMenuItem(i18nManager.getMessage(Messages.MGMT_MENU_DEPLOYMENTS_UPLOAD), new NewDeploymentListener());
  }
  
  protected void addActiveProcessDefinitionsEntry() {
    addToolbarEntry(ENTRY_ACTIVE_PROCESS_DEFINITIONS, 
            i18nManager.getMessage(Messages.MGMT_MENU_ACTIVE_PROCESS_DEFINITIONS), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showActiveProcessDefinitionsPage();
      }
    });
  }
  
  protected void addSuspendedProcessDefinitionsEntry() {
    addToolbarEntry(ENTRY_SUSPENDED_PROCESS_DEFINITIONS, 
            i18nManager.getMessage(Messages.MGMT_MENU_SUSPENDED_PROCESS_DEFINITIONS), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showSuspendedProcessDefinitionsPage();
      }
    });
  }

  protected void addJobsToolbarEntry() {
    addToolbarEntry(ENTRY_JOBS, i18nManager.getMessage(Messages.MGMT_MENU_JOBS), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showJobPage();
      }
    });
  }

  protected void addUsersToolbarEntry() {
    addToolbarEntry(ENTRY_USERS, i18nManager.getMessage(Messages.MGMT_MENU_USERS), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showUserPage();
      }
    });
  }

  protected void addGroupToolbarEntry() {
    addToolbarEntry(ENTRY_GROUPS, i18nManager.getMessage(Messages.MGMT_MENU_GROUPS), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showGroupPage();
      }
    });
  }
  
  protected void addAdministrationToolbarEntry() {
    addToolbarEntry(ENTRY_ADMIN, i18nManager.getMessage(Messages.MGMT_MENU_ADMINISTRATION), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showAdministrationPage();
      }
    });
  }
  
  protected void addCrystalBallToolbarEntry() {
    addToolbarEntry(ENTRY_CRYSTALBALL, i18nManager.getMessage(Messages.MGMT_MENU_CRYSTALBALL), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showCrystalBallPage();
      }
    });
  }
  
}
