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
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.flow.FlowPage;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class DeploymentDetailPanel extends Panel {

  private static final long serialVersionUID = 1L;
  
  protected RepositoryService repositoryService;
  protected Deployment deployment;
  protected DeploymentPage parent;
  
  public DeploymentDetailPanel(String deploymentId, DeploymentPage parent) {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
    this.parent = parent;
    
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    addDeploymentName();
    addDeploymentTime();
    addProcessDefinitionLinks();
    addResourceLinks();
  }
  
  protected void addDeploymentName() {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setSpacing(true);
    addComponent(layout);
    
    // Name
    Label nameLabel = new Label(deployment.getName());
    nameLabel.addStyleName(Reindeer.LABEL_H1);
    layout.addComponent(nameLabel);
    
    // Delete button
    Button deleteButton = new Button("Delete");
    deleteButton.setIcon(Images.DELETE);
    deleteButton.addStyleName(Reindeer.BUTTON_LINK);
    layout.addComponent(deleteButton);
    layout.setComponentAlignment(deleteButton, Alignment.MIDDLE_LEFT);
    deleteButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        ExplorerApplication.getCurrent().showPopupWindow(new DeleteDeploymentPopupWindow(deployment, parent));
      }
    });
  }
  
  protected void addDeploymentTime() {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    addComponent(emptySpace);
    
    HorizontalLayout timeDetails = new HorizontalLayout();
    timeDetails.setSpacing(true);
    addComponent(timeDetails);
    
    Embedded clockImage = new Embedded(null, Images.CLOCK);
    timeDetails.addComponent(clockImage);
    
    Label timeLabel = new Label("Deployed " + new PrettyTime().format(deployment.getDeploymentTime()));
    timeDetails.addComponent(timeLabel);
    timeDetails.setComponentAlignment(timeLabel, Alignment.MIDDLE_CENTER);
  }
  
  protected void addProcessDefinitionLinks() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
      .deploymentId(deployment.getId())
      .orderByProcessDefinitionName().asc()
      .list();
    
    if (processDefinitions.size() > 0) {
      
      // Header
      Label processDefinitionHeader = new Label("Process Definitions");
      processDefinitionHeader.addStyleName(ExplorerLayout.STYLE_DEPLOYMENT_DETAILS_HEADER);
      processDefinitionHeader.setWidth("95%");
      addComponent(processDefinitionHeader);
      
      // layout
      HorizontalLayout layout = new HorizontalLayout();
      layout.setSpacing(true);
      addComponent(layout);
      
      // process icon
      Embedded processIcon = new Embedded(null, Images.PROCESS_48PX);
      layout.addComponent(processIcon);
      
      // processes
      VerticalLayout processDefinitionLinksLayout = new VerticalLayout();
      processDefinitionLinksLayout.setSpacing(true);
      layout.addComponent(processDefinitionLinksLayout);
      layout.setComponentAlignment(processDefinitionLinksLayout, Alignment.MIDDLE_LEFT);
      
      for (final ProcessDefinition processDefinition : processDefinitions) {
        Button processDefinitionButton = new Button(processDefinition.getName());
        processDefinitionButton.addListener(new ClickListener() {
          public void buttonClick(ClickEvent event) {
            ExplorerApplication.getCurrent().switchView(new FlowPage(processDefinition.getId()));
          }
        });
        processDefinitionButton.addStyleName(Reindeer.BUTTON_LINK);
        processDefinitionLinksLayout.addComponent(processDefinitionButton);
      }
    }
  }
  
  protected void addResourceLinks() {
    List<String> resourceNames = repositoryService.getDeploymentResourceNames(deployment.getId());
    Collections.sort(resourceNames); // small nr of elements, so we can do it in-memory
    
    if (resourceNames.size() > 0) {
      Label resourceHeader = new Label("Resources");
      resourceHeader.setWidth("95%");
      resourceHeader.addStyleName(ExplorerLayout.STYLE_DEPLOYMENT_DETAILS_HEADER);
      addComponent(resourceHeader);
      
      HorizontalLayout resourceLayout = new HorizontalLayout();
      addComponent(resourceLayout);
      
      // resource icon
      Embedded resourceIcon = new Embedded(null, Images.RESOURCE);
      resourceLayout.addComponent(resourceIcon);
      
      // resources
      VerticalLayout resourceLinksLayout = new VerticalLayout();
      resourceLayout.setSpacing(true);
      resourceLayout.addComponent(resourceLinksLayout);
      resourceLayout.setComponentAlignment(resourceLinksLayout, Alignment.MIDDLE_LEFT);
      
      for (final String resourceName : resourceNames) {
        StreamResource.StreamSource streamSource = new StreamSource() {
          public InputStream getStream() {
            return repositoryService.getResourceAsStream(deployment.getId(), resourceName);
          }
        };
        Link resourceLink = new Link(resourceName, new StreamResource(streamSource, resourceName, ExplorerApplication.getCurrent()));
        resourceLinksLayout.addComponent(resourceLink);
      }
    }
    
  }

}
