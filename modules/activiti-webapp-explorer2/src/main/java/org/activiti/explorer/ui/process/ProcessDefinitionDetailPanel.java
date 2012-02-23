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
package org.activiti.explorer.ui.process;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.custom.PrettyTimeLabel;
import org.activiti.explorer.ui.form.FormPropertiesEventListener;
import org.activiti.explorer.ui.form.FormPropertiesForm;
import org.activiti.explorer.ui.form.FormPropertiesForm.FormPropertiesEvent;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.process.listener.StartProcessInstanceClickListener;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * Panel showing process definition detail.
 * 
 * @author Frederik Heremans
 */
public class ProcessDefinitionDetailPanel extends DetailPanel {
  
  private static final long serialVersionUID = 1L;
  
  // Members
  protected ProcessDefinition processDefinition;
  protected Deployment deployment;
  protected ProcessDefinitionPage processDefinitionPage;
  
  // Services
  protected RepositoryService repositoryService;
  protected FormService formService; 
  protected I18nManager i18nManager;
  
  // UI
  protected VerticalLayout detailPanelLayout;
  protected HorizontalLayout detailContainer;
  protected HorizontalLayout actionsContainer;
  protected Label nameLabel;
  protected Button startProcessInstanceButton;
  
  protected FormPropertiesForm processDefinitionStartForm;
  protected ProcessDefinitionInfoComponent definitionInfoComponent;
  
  public ProcessDefinitionDetailPanel(String processDefinitionId, ProcessDefinitionPage processDefinitionPage) {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.formService = ProcessEngines.getDefaultProcessEngine().getFormService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    this.processDefinitionPage = processDefinitionPage;
    this.processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();

    if(processDefinition != null) {
      deployment = repositoryService.createDeploymentQuery().deploymentId(processDefinition.getDeploymentId()).singleResult();
    }
    
    initUi();
  }
  
  protected void initUi() {
    setSizeFull();
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    detailPanelLayout = new VerticalLayout();
    detailPanelLayout.setWidth(100, UNITS_PERCENTAGE);
    detailPanelLayout.setMargin(true);
    setDetailContainer(detailPanelLayout);
    
    // All details about the process definition
    initHeader();
    
    detailContainer = new HorizontalLayout();
    detailContainer.addStyleName(Reindeer.PANEL_LIGHT);
    detailPanelLayout.addComponent(detailContainer);
    detailContainer.setSizeFull();
    
    initActions();
    initProcessDefinitionInfo();
  }
  
  protected void initActions() {
    startProcessInstanceButton = new Button(i18nManager.getMessage(Messages.PROCESS_START));
    startProcessInstanceButton.addListener(new StartProcessInstanceClickListener(processDefinition, processDefinitionPage));
    
    // Clear toolbar and add 'start' button
    processDefinitionPage.getToolBar().removeAllButtons();
    processDefinitionPage.getToolBar().addButton(startProcessInstanceButton);
  }
  

  public void initProcessDefinitionInfo() {
    if(definitionInfoComponent == null) {
      definitionInfoComponent = new ProcessDefinitionInfoComponent(processDefinition, deployment);
    }
    
    if (startProcessInstanceButton != null) {
      startProcessInstanceButton.setEnabled(true);
    }
    
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
        private static final long serialVersionUID = 1L;
        protected void handleFormSubmit(FormPropertiesEvent event) {
          formService.submitStartFormData(processDefinition.getId(), event.getFormProperties());
          
          // Show notification
          ExplorerApp.get().getMainWindow().showNotification("Process '" + 
                  getProcessDisplayName(processDefinition) + "' started successfully");
          initProcessDefinitionInfo();
        }
        protected void handleFormCancel(FormPropertiesEvent event) {
          initProcessDefinitionInfo();
        }
      });
    }
    processDefinitionStartForm.setFormProperties(startFormData.getFormProperties());
    
    startProcessInstanceButton.setEnabled(false);
    detailContainer.removeAllComponents();
    detailContainer.addComponent(processDefinitionStartForm);
  }
  
  protected void initHeader() {
    GridLayout details = new GridLayout(2, 2);
    details.setWidth(100, UNITS_PERCENTAGE);
    details.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    details.setSpacing(true);
    details.setMargin(false, false, true, false);
    details.setColumnExpandRatio(1, 1.0f);
    detailPanelLayout.addComponent(details);
    
    // Image
    Embedded image = new Embedded(null, Images.PROCESS_50);
    details.addComponent(image, 0, 0, 0, 1);
    
    // Name
    Label nameLabel = new Label(getProcessDisplayName(processDefinition));
    nameLabel.addStyleName(Reindeer.LABEL_H2);
    details.addComponent(nameLabel, 1, 0);

    // Properties
    HorizontalLayout propertiesLayout = new HorizontalLayout();
    propertiesLayout.setSpacing(true);
    details.addComponent(propertiesLayout);
    
    // Version
    String versionString = i18nManager.getMessage(Messages.PROCESS_VERSION, processDefinition.getVersion());
    Label versionLabel = new Label(versionString);
    versionLabel.addStyleName(ExplorerLayout.STYLE_PROCESS_HEADER_VERSION);
    propertiesLayout.addComponent(versionLabel);
    
    // Add deploy time
    PrettyTimeLabel deployTimeLabel = new PrettyTimeLabel(i18nManager.getMessage(Messages.PROCESS_DEPLOY_TIME),
      deployment.getDeploymentTime(), null, true);
    deployTimeLabel.addStyleName(ExplorerLayout.STYLE_PROCESS_HEADER_DEPLOY_TIME);
    propertiesLayout.addComponent(deployTimeLabel);
  }
  
  protected String getProcessDisplayName(ProcessDefinition processDefinition) {
    if(processDefinition.getName() != null) {
      return processDefinition.getName();
    } else {
      return processDefinition.getKey();
    }
  }
  
  protected void addEmptySpace(ComponentContainer container) {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    container.addComponent(emptySpace);
  }
}
