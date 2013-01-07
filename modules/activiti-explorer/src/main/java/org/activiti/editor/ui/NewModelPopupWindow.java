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
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.PopupWindow;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractLayout;
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
  protected HorizontalLayout modelerLayout;
  protected Label modelerLabel;
  protected HorizontalLayout tableEditorLayout;
  protected Label tableEditorLabel;
  protected boolean modelerPreffered;
  
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
    setWidth("460px");
    setHeight("450px");
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
    
    VerticalLayout editorsLayout = new VerticalLayout();
    formLayout.addComponent(editorsLayout);

    createModelerEditorChoice(editorsLayout);
    createTableDrivenEditorChoice(editorsLayout);
    
    addComponent(formLayout);
    
    preferModeler();
    
    // Some empty space
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    addComponent(emptySpace);
  }


  protected void createModelerEditorChoice(VerticalLayout editorsLayout) {
    modelerLayout = new HorizontalLayout();
    modelerLayout.setWidth("300px");
    modelerLayout.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    editorsLayout.addComponent(modelerLayout);
    
    modelerLayout.addListener(new LayoutClickListener() {
      public void layoutClick(LayoutClickEvent event) {
        preferModeler();
      }
    });
    
    Button modelerButton = new Button();
    modelerButton.setIcon(Images.PROCESS_EDITOR_BPMN);
    modelerButton.setStyleName(Reindeer.BUTTON_LINK);
    modelerLayout.addComponent(modelerButton);
    modelerLayout.setComponentAlignment(modelerButton, Alignment.MIDDLE_LEFT);
    
    modelerButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        preferModeler();
      }
    });
    
    VerticalLayout modelerTextLayout = new VerticalLayout();
    modelerLayout.addComponent(modelerTextLayout);
    modelerLayout.setExpandRatio(modelerTextLayout, 1.0f);
    
    modelerLabel = new Label(i18nManager.getMessage(Messages.PROCESS_EDITOR_MODELER));
    modelerLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    modelerLabel.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    modelerTextLayout.addComponent(modelerLabel);
    
    Label modelerDescriptionLabel = new Label(i18nManager.getMessage(Messages.PROCESS_EDITOR_MODELER_DESCRIPTION));
    modelerDescriptionLabel.addStyleName(Reindeer.LABEL_SMALL);
    modelerDescriptionLabel.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    modelerTextLayout.addComponent(modelerDescriptionLabel);
  }
  
  protected void createTableDrivenEditorChoice(VerticalLayout editorsLayout) {
    tableEditorLayout = new HorizontalLayout();
    tableEditorLayout.setWidth("300px");
    tableEditorLayout.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    editorsLayout.addComponent(tableEditorLayout);
    
    tableEditorLayout.addListener(new LayoutClickListener() {
      public void layoutClick(LayoutClickEvent event) {
        preferTableDrivenEditor();
      }
    });
    
    Button tableEditorButton = new Button();
    tableEditorButton.setIcon(Images.PROCESS_EDITOR_TABLE);
    tableEditorButton.setStyleName(Reindeer.BUTTON_LINK);
    tableEditorLayout.addComponent(tableEditorButton);
    tableEditorLayout.setComponentAlignment(tableEditorButton, Alignment.MIDDLE_LEFT);
    
    tableEditorButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        preferTableDrivenEditor();
      }
    });
    
    VerticalLayout tableEditorTextLayout = new VerticalLayout();
    tableEditorLayout.addComponent(tableEditorTextLayout);
    tableEditorLayout.setExpandRatio(tableEditorTextLayout, 1.0f);
    
    tableEditorLabel = new Label(i18nManager.getMessage(Messages.PROCESS_EDITOR_TABLE));
    tableEditorLabel.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    tableEditorTextLayout.addComponent(tableEditorLabel);
    
    Label tableEditorDescriptionLabel = new Label(i18nManager.getMessage(Messages.PROCESS_EDITOR_TABLE_DESCRIPTION));
    tableEditorDescriptionLabel.addStyleName(Reindeer.LABEL_SMALL);
    tableEditorDescriptionLabel.addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    tableEditorTextLayout.addComponent(tableEditorDescriptionLabel);
  }
  
  protected void preferModeler() {
    if (!modelerPreffered) {
      modelerPreffered = true;
      selectEditor(modelerLayout);
      deselectEditor(tableEditorLayout);
      
      modelerLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
      tableEditorLabel.removeStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    }
  }
  
  protected void preferTableDrivenEditor() {
    if (modelerPreffered) {
      modelerPreffered = false;
      selectEditor(tableEditorLayout);
      deselectEditor(modelerLayout);
      
      tableEditorLabel.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
      modelerLabel.removeStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    }
  }
  
  protected void selectEditor(AbstractLayout editorLayout) {
    editorLayout.addStyleName(ExplorerLayout.STYLE_PROCESS_EDITOR_CHOICE);
  }
  
  protected void deselectEditor(AbstractLayout editorLayout) {
    editorLayout.removeStyleName(ExplorerLayout.STYLE_PROCESS_EDITOR_CHOICE);
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
        
        if (modelerPreffered) {
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
            ExplorerApp.get().getMainWindow().open(new ExternalResource(
                ExplorerApp.get().getURL().toString().replace("/ui", "") + "service/editor?id=" + modelData.getId()));
            
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
