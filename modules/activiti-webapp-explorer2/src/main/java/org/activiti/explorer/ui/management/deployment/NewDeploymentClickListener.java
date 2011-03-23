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

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Joram Barrez
 */
public class NewDeploymentClickListener implements ClickListener {

  private static final long serialVersionUID = 1L;

  public void buttonClick(ClickEvent event) {
    DeploymentUploadReceiver receiver = new DeploymentUploadReceiver();
    UploadPopupWindow uploadPopupWindow = new UploadPopupWindow(
            "Upload new Deployment", 
            "Select a file (.bar, .zip or .bpmn20.xml) or drop a file in the rectangle below",
            receiver);
    
    // The receiver also acts as a listener for the end of the upload
    uploadPopupWindow.addFinishedListener(receiver);
    ExplorerApplication.getCurrent().showPopupWindow(uploadPopupWindow);
  }

}
