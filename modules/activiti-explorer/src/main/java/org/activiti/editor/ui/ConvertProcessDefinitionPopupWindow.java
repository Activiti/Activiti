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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ui.custom.PopupWindow;
import org.activiti.explorer.util.XmlUtil;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Tijs Rademakers
 */
public class ConvertProcessDefinitionPopupWindow extends PopupWindow implements ModelDataJsonConstants {
  
  private static final long serialVersionUID = 1L;
  
  protected transient RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  protected transient RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
  
  protected I18nManager i18nManager;
  protected NotificationManager notificationManager;
  protected VerticalLayout windowLayout;
  protected ProcessDefinition processDefinition;
  
  public ConvertProcessDefinitionPopupWindow(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
    this.windowLayout = (VerticalLayout) getContent();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.notificationManager = ExplorerApp.get().getNotificationManager();
    
    initWindow();
    addConvertWarning();
    addButtons();
  }
  
  protected void initWindow() {
    windowLayout.setSpacing(true);
    addStyleName(Reindeer.WINDOW_LIGHT);
    setModal(true);
    center();
    
    String name = processDefinition.getName();
    if (StringUtils.isEmpty(name)) {
      name = processDefinition.getKey();
    }
    setCaption(i18nManager.getMessage(Messages.PROCESS_CONVERT_POPUP_CAPTION, name));
  }
  
  protected void addConvertWarning() {
    Label convertLabel = new Label(i18nManager.getMessage(Messages.PROCESS_CONVERT_POPUP_MESSAGE));
    convertLabel.addStyleName(Reindeer.LABEL_SMALL);
    addComponent(convertLabel);
    
    // Some empty space
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    addComponent(emptySpace);
  }
  
  protected void addButtons() {
    // Cancel
    Button cancelButton = new Button(i18nManager.getMessage(Messages.BUTTON_CANCEL));
    cancelButton.addStyleName(Reindeer.BUTTON_SMALL);
    cancelButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;
      
      public void buttonClick(ClickEvent event) {
        close();
      }
    });
    
    // Convert
    Button convertButton = new Button(i18nManager.getMessage(Messages.PROCESS_CONVERT_POPUP_CONVERT_BUTTON));
    convertButton.addStyleName(Reindeer.BUTTON_SMALL);
    convertButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        
        try {
          InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getResourceName());
          XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
          InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
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
          
              BpmnJsonConverter converter = new BpmnJsonConverter();
              ObjectNode modelNode = converter.convertToJson(bpmnModel);
              Model modelData = repositoryService.newModel();
              
              ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
              modelObjectNode.put(MODEL_NAME, processDefinition.getName());
              modelObjectNode.put(MODEL_REVISION, 1);
              modelObjectNode.put(MODEL_DESCRIPTION, processDefinition.getDescription());
              modelData.setMetaInfo(modelObjectNode.toString());
              modelData.setName(processDefinition.getName());
              
              repositoryService.saveModel(modelData);
              
              repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));
              
              close();
              ExplorerApp.get().getViewManager().showEditorProcessDefinitionPage(modelData.getId());

	          URL explorerURL = ExplorerApp.get().getURL();
	          URL url = new URL(explorerURL.getProtocol(), explorerURL.getHost(), explorerURL.getPort(),
			          explorerURL.getPath().replace("/ui", "") + "modeler.html?modelId=" + modelData.getId());
              ExplorerApp.get().getMainWindow().open(new ExternalResource(url));
            }
          }
          
        } catch(Exception e) {
          notificationManager.showErrorNotification("error", e);
        }
      }
    });
    
    // Alignment
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setSpacing(true);
    buttonLayout.addComponent(cancelButton);
    buttonLayout.addComponent(convertButton);
    addComponent(buttonLayout);
    windowLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
  }

}
