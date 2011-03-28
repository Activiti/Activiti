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

import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.Messages;

import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;


/**
 * @author Joram Barrez
 */
public class NewDeploymentListener implements Command {

  private static final long serialVersionUID = 1L;
  
  public void menuSelected(MenuItem selectedItem) {
    DeploymentUploadReceiver receiver = new DeploymentUploadReceiver();
    UploadPopupWindow uploadPopupWindow = new UploadPopupWindow(
            ExplorerApplication.getCurrent().getMessage(Messages.DEPLOYMENT_UPLOAD),
            ExplorerApplication.getCurrent().getMessage(Messages.DEPLOYMENT_UPLOAD_DESCRIPTION),
            receiver);
    
    // The receiver also acts as a listener for the end of the upload 
    // so it can switch to the new deployment page
    uploadPopupWindow.addFinishedListener(receiver);
    ExplorerApplication.getCurrent().showPopupWindow(uploadPopupWindow);
  }

}
