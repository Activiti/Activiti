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
package org.activiti.editor.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.util.XmlUtil;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.Receiver;


/**
 * @author Tijs Rademakers
 */
public class ImportUploadReceiver implements Receiver, FinishedListener, ModelDataJsonConstants {

  private static final long serialVersionUID = 1L;
  
  protected transient RepositoryService repositoryService;
  protected I18nManager i18nManager;
  protected NotificationManager notificationManager;
  protected ViewManager viewManager;
  
  // Will be assigned during upload
  protected ByteArrayOutputStream outputStream;
  protected String fileName;
  
  // Will be assigned after deployment
  protected boolean validFile = false;
  protected Model modelData;
  
  public ImportUploadReceiver() {
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
    try {
      try {
        if (fileName.endsWith(".bpmn20.xml") || fileName.endsWith(".bpmn")) {
          validFile = true;
            
          XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
          InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray()), "UTF-8");
          XMLStreamReader xtr = xif.createXMLStreamReader(in);
          BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
          
          if (bpmnModel.getMainProcess() == null || bpmnModel.getMainProcess().getId() == null) {
            notificationManager.showErrorNotification(Messages.MODEL_IMPORT_FAILED, 
                i18nManager.getMessage(Messages.MODEL_IMPORT_INVALID_BPMN_EXPLANATION));
          } else {
          
            if (bpmnModel.getLocationMap().isEmpty()) {
              notificationManager.showErrorNotification(Messages.MODEL_IMPORT_INVALID_BPMNDI,
                  i18nManager.getMessage(Messages.MODEL_IMPORT_INVALID_BPMNDI_EXPLANATION));
            } else {
            
              String processName = null;
              if (StringUtils.isNotEmpty(bpmnModel.getMainProcess().getName())) {
                processName = bpmnModel.getMainProcess().getName();
              } else {
                processName = bpmnModel.getMainProcess().getId();
              }
              
              modelData = repositoryService.newModel();
              ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
              modelObjectNode.put(MODEL_NAME, processName);
              modelObjectNode.put(MODEL_REVISION, 1);
              modelData.setMetaInfo(modelObjectNode.toString());
              modelData.setName(processName);
              
              repositoryService.saveModel(modelData);
              
              BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
              ObjectNode editorNode = jsonConverter.convertToJson(bpmnModel);
              
              repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
            }
          }
        } else {
          notificationManager.showErrorNotification(Messages.MODEL_IMPORT_INVALID_FILE,
          		i18nManager.getMessage(Messages.MODEL_IMPORT_INVALID_FILE_EXPLANATION));
        }
      } catch (Exception e) {
        String errorMsg = e.getMessage().replace(System.getProperty("line.separator"), "<br/>");
        notificationManager.showErrorNotification(Messages.MODEL_IMPORT_FAILED, errorMsg);
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
    viewManager.showEditorProcessDefinitionPage(modelData.getId());
  }
  
}
