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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.ExplorerLayout;
import org.activiti.explorer.ui.Images;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author 'Frederik Heremans'
 */
public class ProcessDefinitionInfoComponent extends VerticalLayout {

  private static final long serialVersionUID = -3523189433414901853L;
  
  // Members
  protected ProcessDefinition processDefinition;
  protected Deployment deployment;
  
  // Services
  protected RepositoryService repositoryService;
  
  // UI
  protected HorizontalLayout timeDetails;
  protected VerticalLayout processImageContainer;
  
  
  public ProcessDefinitionInfoComponent(ProcessDefinition processDefinition, Deployment deployment) {
    super();
    this.processDefinition = processDefinition;
    this.deployment = deployment;
    
    repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    
    initTimeDetails();
    initImage();
  }
  
  protected void initTimeDetails() {
    timeDetails = new HorizontalLayout();
    timeDetails.setSpacing(true);
    timeDetails.setSizeUndefined();
    addComponent(timeDetails);

    Embedded clockImage = new Embedded(null, Images.CLOCK);
    timeDetails.addComponent(clockImage);

    // The other time fields are layed out in a 2 column grid
    GridLayout grid = new GridLayout();
    grid.addStyleName(ExplorerLayout.STYLE_TASK_DETAILS);
    grid.setSpacing(true);
    grid.setColumns(2);

    timeDetails.addComponent(grid);
    timeDetails.setComponentAlignment(grid, Alignment.MIDDLE_LEFT);

    // Version
    Label version = new Label(ExplorerApplication.getCurrent().getMessage(Messages.FLOW_VERSION) + processDefinition.getVersion());
    version.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
    version.setSizeUndefined();
    grid.addComponent(version);
    grid.space();
    
    // Deployment time
    if (deployment.getDeploymentTime() != null) {
      Label createTime = new Label(ExplorerApplication.getCurrent().getMessage(Messages.FLOW_DEPLOY_TIME) + new PrettyTime().format(deployment.getDeploymentTime()));
      createTime.addStyleName(ExplorerLayout.STYLE_LABEL_BOLD);
      createTime.setSizeUndefined();
      grid.addComponent(createTime);
      
      Label realCreateTime = new Label("(" + deployment.getDeploymentTime() + ")");
      realCreateTime.addStyleName(Reindeer.LABEL_SMALL);
      realCreateTime.setSizeUndefined();
      grid.addComponent(realCreateTime);
    }
    
  }
  
  protected void initImage() {
    VerticalLayout processImageContainer = new VerticalLayout();
    
    Label processTitle = new Label(ExplorerApplication.getCurrent().getMessage(Messages.FLOW_HEADER_DIAGRAM));
    processTitle.addStyleName(ExplorerLayout.STYLE_PROCESS_DEFINITION_DETAILS_HEADER);
    processImageContainer.addComponent(processTitle);
    
    if(processDefinition.getDiagramResourceName() != null) {
      StreamResource diagram = new ProcessDefinitionImageStreamResourceBuilder()
        .buildStreamResource(processDefinition, repositoryService);
      
      Embedded embedded = new Embedded("", diagram);
      embedded.setType(Embedded.TYPE_IMAGE);
      processImageContainer.addComponent(embedded);
    } else {
      Label noImageAvailable = new Label(ExplorerApplication.getCurrent().getMessage(Messages.FLOW_NO_DIAGRAM));
      noImageAvailable.addStyleName(Reindeer.LABEL_SMALL);
      processImageContainer.addComponent(noImageAvailable);
    }
    
    addComponent(processImageContainer);
  }
  
  protected void addEmptySpace(ComponentContainer container) {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    container.addComponent(emptySpace);
  }
  
}
