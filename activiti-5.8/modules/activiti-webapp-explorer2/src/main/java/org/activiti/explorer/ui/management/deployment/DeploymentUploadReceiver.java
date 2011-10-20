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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ViewManager;

import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.Receiver;


/**
 * @author Joram Barrez
 */
public class DeploymentUploadReceiver implements Receiver, FinishedListener {

  private static final long serialVersionUID = 1L;
  
  protected RepositoryService repositoryService;
  protected I18nManager i18nManager;
  protected NotificationManager notificationManager;
  protected ViewManager viewManager;
  
  // Will be assigned during upload
  protected ByteArrayOutputStream outputStream;
  protected String fileName;
  
  // Will be assigned after deployment
  protected boolean validFile = false;
  protected Deployment deployment;
  
  public DeploymentUploadReceiver() {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.notificationManager = ExplorerApp.get().getNotificationManager();
    this.viewManager = ExplorerApp.get().getViewManager();
  }
  
  public OutputStream receiveUpload(String filename, String mimeType) {
    this.fileName = filename;
    this.outputStream = new ByteArrayOutputStream();
    return outputStream;
  }

  public void uploadFinished(FinishedEvent event) {
    deployUploadedFile();
    if (validFile) {
      showUploadedDeployment();
    }
  }

  protected void deployUploadedFile() {
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name(fileName);
    try {
      try {
        if (fileName.endsWith(".bpmn20.xml")) {
          validFile = true;
          deployment = deploymentBuilder
            .addInputStream(fileName, new ByteArrayInputStream(outputStream.toByteArray()))
            .deploy();
        } else if (fileName.endsWith(".bar") || fileName.endsWith(".zip")) {
          validFile = true;
          deployment = deploymentBuilder
            .addZipInputStream(new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray())))
            .deploy();
        } else {
          notificationManager.showErrorNotification(Messages.DEPLOYMENT_UPLOAD_INVALID_FILE,
          		i18nManager.getMessage(Messages.DEPLOYMENT_UPLOAD_INVALID_FILE_EXPLANATION));
        }
      } catch (ActivitiException e) {
        String errorMsg = e.getMessage().replace(System.getProperty("line.separator"), "<br/>");
        notificationManager.showErrorNotification(Messages.DEPLOYMENT_UPLOAD_FAILED, errorMsg);
      }
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          notificationManager.showErrorNotification("Server-side error", e.getMessage());
        }
      }
    }
  }
  
  protected void showUploadedDeployment() {
    viewManager.showDeploymentPage(deployment.getId());
  }
  
}
