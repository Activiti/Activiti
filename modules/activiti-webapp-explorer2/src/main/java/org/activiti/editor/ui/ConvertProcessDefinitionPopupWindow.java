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

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.data.dao.ModelDao;
import org.activiti.editor.data.model.ModelData;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.PopupWindow;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

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
  
  protected I18nManager i18nManager;
  protected RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  protected RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
  protected VerticalLayout windowLayout;
  protected ProcessDefinition processDefinition;
  
  public ConvertProcessDefinitionPopupWindow(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
    this.windowLayout = (VerticalLayout) getContent();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
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
        
        InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getResourceName());
        BpmnJsonConverter converter = new BpmnJsonConverter(processDefinition.getResourceName(), bpmnStream);
        ObjectNode modelNode = converter.convertToJson();
        ModelData modelData = new ModelData();
        modelData.setModelEditorJson(modelNode.toString());
        
        ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
        modelObjectNode.put(MODEL_NAME, processDefinition.getName());
        modelObjectNode.put(MODEL_REVISION, 1);
        modelObjectNode.put(MODEL_DESCRIPTION, processDefinition.getDescription());
        modelData.setModelJson(modelObjectNode.toString());
        
        long modelId = new ModelDao().saveModel(modelData);
        close();
        ExplorerApp.get().getViewManager().showEditorProcessDefinitionPage(String.valueOf(modelId));
        ExplorerApp.get().getMainWindow().open(new ExternalResource(
            ExplorerApp.get().getURL().toString() + "service/editor?id=" + modelId));
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
