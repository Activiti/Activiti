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
package org.activiti.explorer.ui.flow;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.ui.flow.listener.StartFlowClickListener;
import org.activiti.explorer.ui.form.FormPropertiesForm;
import org.activiti.explorer.ui.form.FormPropertiesForm.FormPropertiesEvent;
import org.activiti.explorer.ui.form.FormPropertiesEventListener;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.Reindeer;


/**
 * Panel showing process definition detail.
 * 
 * @author Frederik Heremans
 */
public class ProcessDefinitionDetailPanel extends Panel {
  
  private static final long serialVersionUID = -2018798598805436750L;
  
  // Members
  protected Deployment deployment;
  protected FlowPage flowPage;
  
  // Services
  protected RepositoryService repositoryService;
  protected ProcessDefinition processDefinition;
  protected FormService formService; 
  
  // UI
  protected HorizontalLayout detailContainer;
  protected HorizontalLayout actionsContainer;
  protected Label nameLabel;
  protected Label categoryLabel;
  protected Button startFlowButton;
  
  protected FormPropertiesForm processDefinitionStartForm;
  protected ProcessDefinitionInfoComponent definitionInfoComponent;
  
  public ProcessDefinitionDetailPanel(String processDefinitionId, FlowPage flowPage) {
    super();
    setSizeFull();
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    this.flowPage = flowPage;
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.formService = ProcessEngines.getDefaultProcessEngine().getFormService();
    this.processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
    
    if(processDefinition != null) {
      deployment = repositoryService.createDeploymentQuery().deploymentId(processDefinition.getDeploymentId()).singleResult();
    }
    
    // All details about the process definition
    initName();
    initCategory();
    initActions();
    
    detailContainer = new HorizontalLayout();
    detailContainer.addStyleName(Reindeer.PANEL_LIGHT);
    addComponent(detailContainer);
    detailContainer.setSizeFull();
    
    // Show details
    showProcessDefinitionInfo();
  }
  

  public void showProcessDefinitionInfo() {
    if(definitionInfoComponent == null) {
      definitionInfoComponent = new ProcessDefinitionInfoComponent(processDefinition, deployment);
    }
    
    startFlowButton.setEnabled(true);
    detailContainer.removeAllComponents();
    detailContainer.addComponent(definitionInfoComponent);
  }
  
  public void showProcessStartForm(StartFormData startFormData) {
    if(processDefinitionStartForm == null) {
      processDefinitionStartForm = new FormPropertiesForm();
      processDefinitionStartForm.setSubmitButtonCaption("Start process");
      processDefinitionStartForm.setCancelButtonCaption("Cancel");
      
      // When form is submitted/cancelled, show the info again
      processDefinitionStartForm.addListener(new FormPropertiesEventListener() {
        
        private static final long serialVersionUID = -1747717959106153970L;

        @Override
        protected void handleFormSubmit(FormPropertiesEvent event) {
          formService.submitStartFormData(processDefinition.getId(), event.getFormProperties());
          
          // Show notification
          ExplorerApplication.getCurrent().getMainWindow().showNotification("Process '" + 
            processDefinition.getName() + "' started successfully");
          showProcessDefinitionInfo();
        }
        
        @Override
        protected void handleFormCancel(FormPropertiesEvent event) {
          showProcessDefinitionInfo();
        }
      });
    }
    processDefinitionStartForm.setFormProperties(startFormData.getFormProperties());
    
    startFlowButton.setEnabled(false);
    detailContainer.removeAllComponents();
    detailContainer.addComponent(processDefinitionStartForm);
  }


  protected void initActions() {
    actionsContainer = new HorizontalLayout();
    actionsContainer.addStyleName(Constants.STYLE_ACTION_BAR);
    actionsContainer.setSizeFull();
    actionsContainer.setSpacing(true);
    
    startFlowButton = new Button("Start flow");
    startFlowButton.addListener(new StartFlowClickListener(processDefinition, flowPage));
    
    actionsContainer.addComponent(startFlowButton);
    
    addComponent(actionsContainer);
    addEmptySpace(this);
  }


  protected void initName() {
    nameLabel = new Label(processDefinition.getName());
    nameLabel.addStyleName(Reindeer.LABEL_H1);
    addComponent(nameLabel);
  }
  
  protected void initCategory() {
    if(processDefinition.getCategory() != null) {
      categoryLabel = new Label("Category: " + processDefinition.getCategory());
      categoryLabel.addStyleName(Reindeer.LABEL_SMALL);
      addComponent(categoryLabel);      
    }
    
    addEmptySpace(this);
  }
  
 
  protected void addEmptySpace(ComponentContainer container) {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    container.addComponent(emptySpace);
  }
}
