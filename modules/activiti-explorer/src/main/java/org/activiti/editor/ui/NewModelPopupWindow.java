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

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ui.custom.PopupWindow;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Tijs Rademakers
 */
public class NewModelPopupWindow extends PopupWindow implements ModelDataJsonConstants {
  
  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  protected NotificationManager notificationManager;
  protected VerticalLayout windowLayout;
  protected Form form;
  protected TextField nameTextField;
  protected TextArea descriptionTextArea;
  
  protected RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  
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
    setWidth("400px");
    setHeight("390px");
    center();
    setCaption(i18nManager.getMessage(Messages.PROCESS_NEW_POPUP_CAPTION));
  }
  
  protected void addFields() {
    form = new Form();
    form.setCaption(i18nManager.getMessage(Messages.PROCESS_NEW_POPUP_CAPTION));
    form.getLayout().setMargin(true);
    
    nameTextField = new TextField(i18nManager.getMessage(Messages.TASK_NAME));
    nameTextField.setWidth(20, Sizeable.UNITS_EM);
    nameTextField.setRequired(true);
    form.getLayout().addComponent(nameTextField);
    nameTextField.focus();
    
    descriptionTextArea = new TextArea(i18nManager.getMessage(Messages.TASK_DESCRIPTION));
    descriptionTextArea.setRows(8);
    descriptionTextArea.setWidth(20, Sizeable.UNITS_EM);
    form.getLayout().addComponent(descriptionTextArea);
    
    addComponent(form);
    
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
    
    // Create
    Button createButton = new Button(i18nManager.getMessage(Messages.PROCESS_NEW_POPUP_CREATE_BUTTON));
    createButton.addStyleName(Reindeer.BUTTON_SMALL);
    createButton.addListener(new ClickListener() {
      
      private static final long serialVersionUID = 1L;

      public void buttonClick(ClickEvent event) {
        
        if (StringUtils.isEmpty((String) nameTextField.getValue())) {
          form.setComponentError(new UserError("The name field is required."));
          return;
        }
        
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
          
          repositoryService.saveModel(modelData);
          
          repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
          
          close();
          ExplorerApp.get().getViewManager().showEditorProcessDefinitionPage(modelData.getId());
          ExplorerApp.get().getMainWindow().open(new ExternalResource(
              ExplorerApp.get().getURL().toString() + "service/editor?id=" + modelData.getId()));
          
        } catch(Exception e) {
          notificationManager.showErrorNotification("error", e);
        }
      }
    });
    
    // Alignment
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setSpacing(true);
    buttonLayout.addComponent(cancelButton);
    buttonLayout.addComponent(createButton);
    addComponent(buttonLayout);
    windowLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
  }

}
