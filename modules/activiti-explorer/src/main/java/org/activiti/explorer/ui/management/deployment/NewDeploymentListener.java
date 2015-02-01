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
package org.activiti.explorer.ui.management.deployment;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.custom.ToolbarEntry.ToolbarCommand;
import org.activiti.explorer.ui.custom.UploadPopupWindow;


/**
 * @author Joram Barrez
 */
public class NewDeploymentListener implements ToolbarCommand {

  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  
  public NewDeploymentListener() {
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
  }
  
  public void toolBarItemSelected() {
    DeploymentUploadReceiver receiver = new DeploymentUploadReceiver();
    UploadPopupWindow uploadPopupWindow = new UploadPopupWindow(
            i18nManager.getMessage(Messages.DEPLOYMENT_UPLOAD),
            i18nManager.getMessage(Messages.DEPLOYMENT_UPLOAD_DESCRIPTION),
            receiver);
    
    // The receiver also acts as a listener for the end of the upload 
    // so it can switch to the new deployment page
    uploadPopupWindow.addFinishedListener(receiver);
    viewManager.showPopupWindow(uploadPopupWindow);
  }
}
