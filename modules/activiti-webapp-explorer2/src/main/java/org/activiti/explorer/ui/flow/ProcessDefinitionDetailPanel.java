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
import org.activiti.explorer.Constants;
import org.activiti.explorer.Images;
import org.activiti.explorer.ui.ProcessDefinitionImageStreamResourceBuilder;
import org.activiti.explorer.ui.flow.listener.StartFlowClickListener;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.Reindeer;


/**
 * Panel showing process definition detail.
 * 
 * @author Frederik Heremans
 */
public class ProcessDefinitionDetailPanel extends HorizontalLayout {
  
  private static final long serialVersionUID = -2018798598805436750L;
  
  protected RepositoryService repositoryService;
  protected ProcessDefinition processDefinition;
  protected Deployment deployment;
  
  protected Panel detailPanel;
  
  public ProcessDefinitionDetailPanel(String processDefinitionId) {
    super();
    setSizeFull();
    addStyleName(Reindeer.LAYOUT_WHITE);
    
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
    
    if(processDefinition != null) {
      deployment = repositoryService.createDeploymentQuery().deploymentId(processDefinition.getDeploymentId()).singleResult();
    }
    
    // All details about the process definition
    this.detailPanel = new Panel();
    detailPanel.addStyleName(Reindeer.PANEL_LIGHT);
    addComponent(detailPanel);
    setExpandRatio(detailPanel, 1.0f);
    
    initName();
    initCategory();
    initActions();
    initTimeDetails();
    initImage();
  }
  

  protected void initActions() {
    HorizontalLayout actionsContainer = new HorizontalLayout();
    actionsContainer.addStyleName(Constants.STYLE_ACTION_BAR);
    actionsContainer.setSizeFull();
    actionsContainer.setSpacing(true);
    
    Button startFlowAction = new Button("Start flow");
    startFlowAction.addListener(new StartFlowClickListener(processDefinition));
    
    actionsContainer.addComponent(startFlowAction);
    
    detailPanel.addComponent(actionsContainer);
    addEmptySpace(detailPanel);
  }


  protected void initName() {
    Label nameLabel = new Label(processDefinition.getName());
    nameLabel.addStyleName(Reindeer.LABEL_H1);
    detailPanel.addComponent(nameLabel);
  }
  
  protected void initCategory() {
    if(processDefinition.getCategory() != null) {
      Label nameLabel = new Label("Category: " + processDefinition.getCategory());
      nameLabel.addStyleName(Reindeer.LABEL_SMALL);
      detailPanel.addComponent(nameLabel);      
    }
    
    addEmptySpace(detailPanel);
  }
  
  protected void initTimeDetails() {
    HorizontalLayout timeDetailsLayout = new HorizontalLayout();
    timeDetailsLayout.setSpacing(true);
    timeDetailsLayout.setSizeUndefined();
    detailPanel.addComponent(timeDetailsLayout);

    Embedded clockImage = new Embedded(null, Images.CLOCK);
    timeDetailsLayout.addComponent(clockImage);

    // The other time fields are layed out in a 2 column grid
    GridLayout grid = new GridLayout();
    grid.addStyleName(Constants.STYLE_TASK_DETAILS);
    grid.setSpacing(true);
    grid.setColumns(2);

    timeDetailsLayout.addComponent(grid);
    timeDetailsLayout.setComponentAlignment(grid, Alignment.MIDDLE_LEFT);

    // Version
    Label version = new Label("Version " + processDefinition.getVersion());
    version.addStyleName(Constants.STYLE_LABEL_BOLD);
    version.setSizeUndefined();
    grid.addComponent(version);
    grid.space();
    
    // Deployment time
    if (deployment.getDeploymentTime() != null) {
      Label createTime = new Label("Deployed " + new PrettyTime().format(deployment.getDeploymentTime()));
      createTime.addStyleName(Constants.STYLE_LABEL_BOLD);
      createTime.setSizeUndefined();
      grid.addComponent(createTime);
      
      Label realCreateTime = new Label("(" + deployment.getDeploymentTime() + ")");
      realCreateTime.addStyleName(Reindeer.LABEL_SMALL);
      realCreateTime.setSizeUndefined();
      grid.addComponent(realCreateTime);
    }
    
    addEmptySpace(detailPanel);
  }
  
  protected void initImage() {
    Label processTitle = new Label("Flow Image");
    processTitle.addStyleName(Reindeer.LABEL_H2);
    detailPanel.addComponent(processTitle);
    
    if(processDefinition.getDiagramResourceName() != null) {
      StreamResource diagram = new ProcessDefinitionImageStreamResourceBuilder()
        .buildStreamResource(processDefinition, repositoryService);
      
 
      Embedded embedded = new Embedded("", diagram);
      embedded.setType(Embedded.TYPE_IMAGE);
      detailPanel.addComponent(embedded);
    } else {
      Label noImageAvailable = new Label("No image available for this flow");
      detailPanel.addComponent(noImageAvailable);
    }
  }
  
  protected void addEmptySpace(AbstractComponentContainer container) {
    Label emptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
    emptySpace.setSizeUndefined();
    container.addComponent(emptySpace);
  }
}
