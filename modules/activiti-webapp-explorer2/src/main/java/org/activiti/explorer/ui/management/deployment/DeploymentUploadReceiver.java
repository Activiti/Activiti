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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.explorer.ExplorerApplication;

import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.Receiver;


/**
 * @author Joram Barrez
 */
public class DeploymentUploadReceiver implements Receiver, FinishedListener {

  private static final long serialVersionUID = 1L;
  
  protected RepositoryService repositoryService;
  protected ByteArrayOutputStream outputStream;
  protected String fileName;
  
  public DeploymentUploadReceiver() {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  }
  
  public OutputStream receiveUpload(String filename, String mimeType) {
    this.fileName = filename;
    this.outputStream = new ByteArrayOutputStream();
    return outputStream;
  }

  public void uploadFinished(FinishedEvent event) {
    // When upload is finished, we should switch the screen to this freshly deployed deployment
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name(fileName);
    try {
      if (fileName.endsWith(".bpmn20.xml")) {
        deploymentBuilder.addInputStream(fileName, new ByteArrayInputStream(outputStream.toByteArray())).deploy();
      } else if (fileName.endsWith(".bar") || fileName.endsWith(".zip")) {
        deploymentBuilder.addZipInputStream(new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()))).deploy();
      } else {
        ExplorerApplication.getCurrent().showErrorNotification("Could not upload file",
        		"Only .bar, .zip and .bpmn20.xml files are supported");
      }
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          ExplorerApplication.getCurrent().showErrorNotification("Server-side error", e.getMessage());
        }
      }
    }
    
    // Indicate that the deployment has been done
    ExplorerApplication.getCurrent().showInformationNotification(fileName + " has been successfully deployed");
  }
  
}
