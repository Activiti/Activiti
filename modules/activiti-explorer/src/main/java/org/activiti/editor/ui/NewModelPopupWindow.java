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

import java.net.URL;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ui.custom.PopupWindow;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class NewModelPopupWindow extends PopupWindow implements ModelDataJsonConstants {
  
  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  protected NotificationManager notificationManager;
  protected VerticalLayout windowLayout;
  protected GridLayout formLayout;
  protected TextField nameTextField;
  protected TextArea descriptionTextArea;
  protected SelectEditorComponent selectEditorComponent;
  
  protected transient RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  
  public NewModelPopupWindow() {
    this.windowLayout = (VerticalLayout) getContent();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.notificationManager = ExplorerApp.get().getNotificationManager();
    
    initWindow();
    addFields();
    addButtons();
  }
  
  protected void initWindow() {
    windowLayout.setSpacing(true);
    addStyleName(Reindeer.WINDOW_LIGHT);
    setModal(true);
    setWidth("460px");
    setHeight("470px");
    center();
    setCaption(i18nManager.getMessage(Messages.PROCESS_NEW_POPUP_CAPTION));
  }
  
  protected void addFields() {
    formLayout = new GridLayout(2, 3);
    formLayout.setSpacing(true);

    formLayout.addComponent(new Label(i18nManager.getMessage(Messages.TASK_NAME)));
    nameTextField = new TextField();
    nameTextField.setWidth(25, Sizeable.UNITS_EM);
    nameTextField.focus();
    formLayout.addComponent(nameTextField);
    
    formLayout.addComponent(new Label(i18nManager.getMessage(Messages.TASK_DESCRIPTION)));
    descriptionTextArea = new TextArea();
    descriptionTextArea.setRows(8);
    descriptionTextArea.setWidth(25, Sizeable.UNITS_EM);
    descriptionTextArea.addStyleName(ExplorerLayout.STYLE_TEXTAREA_NO_RESIZE);
    formLayout.addComponent(descriptionTextArea);
    
    Label editorLabel = new Label(i18nManager.getMessage(Messages.PROCESS_EDITOR_CHOICE));
    formLayout.addComponent(editorLabel);
    formLayout.setComponentAlignment(editorLabel, Alignment.MIDDLE_LEFT);
    
    selectEditorComponent = new SelectEditorComponent();
    formLayout.addComponent(selectEditorComponent);
    
    addComponent(formLayout);
    
    // Some empty space
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    addComponent(emptySpace);
  }

  protected void addButtons() {
    
    // Create
    Button createButton = new Button(i18nManager.getMessage(Messages.PROCESS_NEW_POPUP_CREATE_BUTTON));
    createButton.setWidth("200px");
    createButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        
        if (StringUtils.isEmpty((String) nameTextField.getValue())) {
          nameTextField.setComponentError(new UserError("The name field is required."));
          return;
        }
        
        if (selectEditorComponent.isModelerPreferred()) {
          try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            Model modelData = repositoryService.newModel();
            
            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(MODEL_NAME, (String) nameTextField.getValue());
            modelObjectNode.put(MODEL_REVISION, 1);
            String description = null;
            if (StringUtils.isNotEmpty((String) descriptionTextArea.getValue())) {
              description = (String) descriptionTextArea.getValue();
            } else {
              description = "";
            }
            modelObjectNode.put(MODEL_DESCRIPTION, description);
            modelData.setMetaInfo(modelObjectNode.toString());
            modelData.setName((String) nameTextField.getValue());
            
            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
            
            close();
            
            ExplorerApp.get().getViewManager().showEditorProcessDefinitionPage(modelData.getId());
	          URL explorerURL = ExplorerApp.get().getURL();
	          URL url = new URL(explorerURL.getProtocol(), explorerURL.getHost(), explorerURL.getPort(),
					          explorerURL.getPath().replace("/ui", "") + "modeler.html?modelId=" + modelData.getId());
            ExplorerApp.get().getMainWindow().open(new ExternalResource(url));
            
          } catch(Exception e) {
            notificationManager.showErrorNotification("error", e);
          }
        } else {
          
          close();
          ExplorerApp.get().getViewManager().showSimpleTableProcessEditor(
                  (String) nameTextField.getValue(), (String) descriptionTextArea.getValue());
          
        }
      }
    });
    
    // Alignment
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setSpacing(true);
    buttonLayout.addComponent(createButton);
    addComponent(buttonLayout);
    windowLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
  }

}
