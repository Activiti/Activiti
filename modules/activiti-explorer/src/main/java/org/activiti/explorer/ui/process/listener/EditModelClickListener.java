package org.activiti.explorer.ui.process.listener;

import java.io.IOException;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ui.process.simple.editor.SimpleTableEditorConstants;
import org.activiti.workflow.simple.converter.json.JsonConverter;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class EditModelClickListener implements ClickListener {

  private static final long serialVersionUID = 1L;
  
  protected Model model;
  protected NotificationManager notificationManager;
  
  public EditModelClickListener(Model model) {
    this.notificationManager = ExplorerApp.get().getNotificationManager(); 
    this.model = model;
  }

  public void buttonClick(ClickEvent event) {
    
    // Check if we must present the option to the user to choose between editors
    if (SimpleTableEditorConstants.TABLE_EDITOR_CATEGORY.equals(model.getCategory())) {
      
      // TODO: option to select between editors
      
      try {
        WorkflowDefinition workflowDefinition = loadWorkflowDefinition();
        ExplorerApp.get().getViewManager().showSimpleTableProcessEditor(model.getId(), workflowDefinition);
      } catch (Exception e) {
        e.printStackTrace();
        ExplorerApp.get().getNotificationManager().showErrorNotification(Messages.PROCESS_EDITOR_LOADING_ERROR, e);
      }
      
    } else { // Modeler by default
      ExplorerApp.get().getMainWindow().open(new ExternalResource(
              ExplorerApp.get().getURL().toString().replace("/ui", "") + "service/editor?id=" + model.getId()));
    }
  }
  
  protected WorkflowDefinition loadWorkflowDefinition() throws JsonProcessingException, IOException {
    RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(repositoryService.getModelEditorSource(model.getId()));
    
    JsonConverter jsonConverter = new JsonConverter();
    return jsonConverter.convertFromJson(jsonNode);
  }
  
}
