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

import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.Images;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class DeploymentDetailPanel extends VerticalLayout {

  private static final long serialVersionUID = 1L;
  
  protected RepositoryService repositoryService;
  protected Deployment deployment;
  
  public DeploymentDetailPanel(String deploymentId) {
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
    
    setSpacing(true);
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    addDeploymentName();
    addDeploymentTime();
    addProcessDefinitionLinks();
    addResourceLinks();
  }
  
  protected void addDeploymentName() {
    Label nameLabel = new Label(deployment.getName());
    nameLabel.addStyleName(Reindeer.LABEL_H1);
    addComponent(nameLabel);
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
      .list();
    
    if (processDefinitions.size() > 0) {
      HorizontalLayout processDefinitionLayout = new HorizontalLayout();
      processDefinitionLayout.setSpacing(true);
      addComponent(processDefinitionLayout);
      
      // process icon
      Embedded processIcon = new Embedded(null, Images.PROCESS_48);
      processDefinitionLayout.addComponent(processIcon);
      
      // processes
      VerticalLayout processDefinitionLinksLayout = new VerticalLayout();
      processDefinitionLayout.addComponent(processDefinitionLinksLayout);
      
      for (ProcessDefinition processDefinition : processDefinitions) {
        Label processDefinitionLabel = new Label(processDefinition.getName());
        processDefinitionLinksLayout.addComponent(processDefinitionLabel);
      }
    }
  }
  
  protected void addResourceLinks() {
    List<String> resourceNames = repositoryService.getDeploymentResourceNames(deployment.getId());
    
    if (resourceNames.size() > 0) {
      HorizontalLayout resourceLayout = new HorizontalLayout();
      addComponent(resourceLayout);
      
      // resource icon
      Embedded resourceIcon = new Embedded(null, Images.RESOURCE);
      resourceLayout.addComponent(resourceIcon);
      
      // resources
      VerticalLayout resourceLinksLayout = new VerticalLayout();
      resourceLayout.addComponent(resourceLinksLayout);
      
      for (String resourceName : resourceNames) {
        resourceLinksLayout.addComponent(new Label(resourceName));
      }
    }
    
  }

}
