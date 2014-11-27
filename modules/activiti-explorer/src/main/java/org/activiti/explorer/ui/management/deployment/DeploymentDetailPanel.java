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
package org.activiti.explorer.ui.management.deployment;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.custom.PrettyTimeLabel;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class DeploymentDetailPanel extends DetailPanel {

  private static final long serialVersionUID = 1L;
  
  protected transient RepositoryService repositoryService;
  protected ViewManager viewManager;
  protected I18nManager i18nManager;
  
  protected Deployment deployment;
  protected DeploymentPage parent;
  
  public DeploymentDetailPanel(String deploymentId, DeploymentPage parent) {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.viewManager = ExplorerApp.get().getViewManager();
    
    this.deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
    this.parent = parent;
    
    init();
  }
  
  protected void init() {
    
    setWidth(100, UNITS_PERCENTAGE);
    
    addDeploymentName();
    addProcessDefinitionLinks();
    addResourceLinks();
    
    addActions();
  }
  
  protected void addActions() {
    // Delete button
    Button deleteButton = new Button(i18nManager.getMessage(Messages.DEPLOYMENT_DELETE));
    deleteButton.setIcon(Images.DELETE);
    deleteButton.addListener(new ClickListener() {

      public void buttonClick(ClickEvent event) {
        viewManager.showPopupWindow(new DeleteDeploymentPopupWindow(deployment, parent));
      }
    });
    
    parent.getToolBar().removeAllButtons();
    parent.getToolBar().addButton(deleteButton);
  }

  protected void addDeploymentName() {

    GridLayout taskDetails = new GridLayout(3, 2);
    taskDetails.setWidth(100, UNITS_PERCENTAGE);
    taskDetails.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    taskDetails.setSpacing(true);
    taskDetails.setMargin(false, false, true, false);
    
    // Add image
    Embedded image = new Embedded(null, Images.DEPLOYMENT_50);
    taskDetails.addComponent(image, 0, 0, 0, 1);
    
    // Add deployment name
    Label nameLabel = new Label();
    if(deployment.getName() != null) {
      nameLabel.setValue(deployment.getName());
    } else {
      nameLabel.setValue(i18nManager.getMessage(Messages.DEPLOYMENT_NO_NAME));
    }
    nameLabel.addStyleName(Reindeer.LABEL_H2);
    taskDetails.addComponent(nameLabel, 1, 0, 2, 0);
    
    // Add deploy time
    PrettyTimeLabel deployTimeLabel = new PrettyTimeLabel(i18nManager.getMessage(Messages.DEPLOYMENT_DEPLOY_TIME),
      deployment.getDeploymentTime(), null, true);
    deployTimeLabel.addStyleName(ExplorerLayout.STYLE_DEPLOYMENT_HEADER_DEPLOY_TIME);
    taskDetails.addComponent(deployTimeLabel, 1, 1);
    
    taskDetails.setColumnExpandRatio(1, 1.0f);
    taskDetails.setColumnExpandRatio(2, 1.0f);
    
    addDetailComponent(taskDetails);
  }
  
  protected void addProcessDefinitionLinks() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
      .deploymentId(deployment.getId())
      .orderByProcessDefinitionName().asc()
      .list();
    
    if (!processDefinitions.isEmpty()) {
      
      // Header
      Label processDefinitionHeader = new Label(i18nManager.getMessage(Messages.DEPLOYMENT_HEADER_DEFINITIONS));
      processDefinitionHeader.addStyleName(ExplorerLayout.STYLE_H3);
      processDefinitionHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
      processDefinitionHeader.setWidth(100, UNITS_PERCENTAGE);
      addDetailComponent(processDefinitionHeader);
      
      // processes
      VerticalLayout processDefinitionLinksLayout = new VerticalLayout();
      processDefinitionLinksLayout.setSpacing(true);
      processDefinitionLinksLayout.setMargin(true, false, true, false);
      addDetailComponent(processDefinitionLinksLayout);
      
      for (final ProcessDefinition processDefinition : processDefinitions) {
        Button processDefinitionButton = new Button(getProcessDisplayName(processDefinition));
        processDefinitionButton.addListener(new ClickListener() {
          public void buttonClick(ClickEvent event) {
            viewManager.showDeployedProcessDefinitionPage(processDefinition.getId());
          }
        });
        processDefinitionButton.addStyleName(Reindeer.BUTTON_LINK);
        processDefinitionLinksLayout.addComponent(processDefinitionButton);
      }
    }
  }
  
  protected String getProcessDisplayName(ProcessDefinition processDefinition) {
    if(processDefinition.getName() != null) {
      return processDefinition.getName();
    } else {
      return processDefinition.getKey();
    }
  }
  
  protected void addResourceLinks() {
    List<String> resourceNames = repositoryService.getDeploymentResourceNames(deployment.getId());
    Collections.sort(resourceNames); // small nr of elements, so we can do it in-memory
    
    if (!resourceNames.isEmpty()) {
      Label resourceHeader = new Label(i18nManager.getMessage(Messages.DEPLOYMENT_HEADER_RESOURCES));
      resourceHeader.setWidth("95%");
      resourceHeader.addStyleName(ExplorerLayout.STYLE_H3);
      resourceHeader.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
      addDetailComponent(resourceHeader);
      
      // resources
      VerticalLayout resourceLinksLayout = new VerticalLayout();
      resourceLinksLayout.setSpacing(true);
      resourceLinksLayout.setMargin(true, false, false, false);
      addDetailComponent(resourceLinksLayout);
      
      for (final String resourceName : resourceNames) {
        StreamResource.StreamSource streamSource = new StreamSource() {
          public InputStream getStream() {
            return repositoryService.getResourceAsStream(deployment.getId(), resourceName);
          }
        };
        Link resourceLink = new Link(resourceName, new StreamResource(streamSource, resourceName, ExplorerApp.get()));
        resourceLinksLayout.addComponent(resourceLink);
      }
    }
  }
}
