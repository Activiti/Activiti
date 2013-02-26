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
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.AbstractPage;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.custom.PrettyTimeLabel;
import org.activiti.explorer.ui.form.FormPropertiesForm;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

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
 * @author Joram Barrez
 */
public abstract class AbstractProcessDefinitionDetailPanel extends DetailPanel {
  
  private static final long serialVersionUID = 1L;
  
  // Members
  protected ProcessDefinition processDefinition;
  protected Deployment deployment;
  protected AbstractPage parentPage;
  
  // Services
  protected transient RepositoryService repositoryService;
  protected transient ManagementService managementService;
  protected transient FormService formService; 
  protected I18nManager i18nManager;
  
  // UI
  protected VerticalLayout detailPanelLayout;
  protected HorizontalLayout detailContainer;
  protected HorizontalLayout actionsContainer;
  protected Label nameLabel;
  
  protected FormPropertiesForm processDefinitionStartForm;
  protected ProcessDefinitionInfoComponent definitionInfoComponent;
  
  public AbstractProcessDefinitionDetailPanel(String processDefinitionId, AbstractPage parentPage) {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.managementService = ProcessEngines.getDefaultProcessEngine().getManagementService();
    this.formService = ProcessEngines.getDefaultProcessEngine().getFormService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    this.parentPage = parentPage;
    this.processDefinition = repositoryService.getProcessDefinition(processDefinitionId);

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
    
    initActions(parentPage);
    initProcessDefinitionInfo();
  }
  
  /**
   * Custom toolbar buttons are added here
   */
  protected abstract void initActions(AbstractPage parentPage);
  

  public void initProcessDefinitionInfo() {
    if(definitionInfoComponent == null) {
      definitionInfoComponent = new ProcessDefinitionInfoComponent(processDefinition, deployment);
    }
    
    detailContainer.removeAllComponents();
    detailContainer.addComponent(definitionInfoComponent);
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
