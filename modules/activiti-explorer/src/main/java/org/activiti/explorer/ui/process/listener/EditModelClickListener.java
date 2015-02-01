package org.activiti.explorer.ui.process.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.editor.ui.SelectEditorComponent;
import org.activiti.editor.ui.SelectEditorComponent.EditorSelectedListener;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ui.custom.PopupWindow;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.process.simple.editor.SimpleTableEditorConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class EditModelClickListener implements ClickListener {

  private static final long serialVersionUID = 1L;
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(EditModelClickListener.class);
  
  protected Model model;
  protected NotificationManager notificationManager;
  
  public EditModelClickListener(Model model) {
    this.notificationManager = ExplorerApp.get().getNotificationManager(); 
    this.model = model;
  }

  public void buttonClick(ClickEvent event) {
    if (SimpleTableEditorConstants.TABLE_EDITOR_CATEGORY.equals(model.getCategory())) {
      showSelectEditorPopupWindow();
    } else {
	    try {
		    showModeler();
	    } catch (MalformedURLException e) {
	      LOGGER.error("Error showing modeler", e);
		    ExplorerApp.get().getNotificationManager().showErrorNotification(Messages.PROCESS_EDITOR_LOADING_ERROR, e);
	    }
    }
  }

  protected WorkflowDefinition loadWorkflowDefinition() throws JsonProcessingException, IOException {
    RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    return ExplorerApp.get().getSimpleWorkflowJsonConverter().readWorkflowDefinition(repositoryService.getModelEditorSource(model.getId()));
  }
  
  protected void showSelectEditorPopupWindow() {
    final PopupWindow selectEditorPopupWindow = new PopupWindow();
    selectEditorPopupWindow.setModal(true);
    selectEditorPopupWindow.setResizable(false);
    selectEditorPopupWindow.setWidth("350px");
    selectEditorPopupWindow.setHeight("250px");
    selectEditorPopupWindow.addStyleName(Reindeer.PANEL_LIGHT);
    selectEditorPopupWindow.center();
    
    final SelectEditorComponent selectEditorComponent = new SelectEditorComponent(false);
    selectEditorComponent.getModelerDescriptionLabel().setValue(
            ExplorerApp.get().getI18nManager().getMessage(Messages.PROCESS_EDITOR_CONVERSION_WARNING_MODELER));
    selectEditorComponent.getModelerDescriptionLabel().addStyleName(ExplorerLayout.STYLE_LABEL_RED);
    selectEditorComponent.preferTableDrivenEditor();
    selectEditorPopupWindow.getContent().addComponent(selectEditorComponent);
    
    selectEditorComponent.setEditorSelectedListener(new EditorSelectedListener() {
      public void editorSelectionChanged() {
        
        try {
          WorkflowDefinition workflowDefinition = loadWorkflowDefinition();
        
          // When using the modeler, the format must first be converted to the modeler json format
          if (selectEditorComponent.isModelerPreferred()) {
             
            WorkflowDefinitionConversion conversion = ExplorerApp.get()
                    .getWorkflowDefinitionConversionFactory().createWorkflowDefinitionConversion(workflowDefinition);
            conversion.convert();
            
            RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
            model.setCategory(null);
            
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode metaInfoJson = objectMapper.createObjectNode();
            metaInfoJson.put("name", model.getName());
            model.setMetaInfo(metaInfoJson.toString());
            repositoryService.saveModel(model);
            
            BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
            ObjectNode json = bpmnJsonConverter.convertToJson(conversion.getBpmnModel());
            repositoryService.addModelEditorSource(model.getId(), json.toString().getBytes("utf-8"));
            
            // Show modeler
            showModeler();
            
          } else {
            
            // Load and show table editor
            ExplorerApp.get().getViewManager().showSimpleTableProcessEditor(model.getId(), workflowDefinition);
            
          }
        } catch (Exception e) {
          LOGGER.error("Error showing editor", e);
          ExplorerApp.get().getNotificationManager().showErrorNotification(Messages.PROCESS_EDITOR_LOADING_ERROR, e);
        } finally {
          ExplorerApp.get().getMainWindow().removeWindow(selectEditorPopupWindow);
        }
        
      }
    });
    
    ExplorerApp.get().getViewManager().showPopupWindow(selectEditorPopupWindow);
  }
  
  protected void showModeler() throws MalformedURLException {
	  URL explorerURL = ExplorerApp.get().getURL();
	  URL url = new URL(explorerURL.getProtocol(), explorerURL.getHost(), explorerURL.getPort(),
			  explorerURL.getPath().replace("/ui",  "") + "modeler.html?modelId=" + model.getId());
    ExplorerApp.get().getMainWindow().open(new ExternalResource(url));
  }
  
}
