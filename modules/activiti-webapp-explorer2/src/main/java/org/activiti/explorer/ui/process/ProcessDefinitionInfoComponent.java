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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Frederik Heremans
 */
public class ProcessDefinitionInfoComponent extends VerticalLayout {

  private static final long serialVersionUID = 1L;

  // Services
  protected RepositoryService repositoryService;
  protected I18nManager i18nManager;
  
  // Members
  protected ProcessDefinition processDefinition;
  protected Deployment deployment;
  
  // UI
  protected HorizontalLayout timeDetails;
  protected VerticalLayout processImageContainer;
  
  
  public ProcessDefinitionInfoComponent(ProcessDefinition processDefinition, Deployment deployment) {
    super();
    this.repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
    this.i18nManager = ExplorerApp.get().getI18nManager(); 
    
    this.processDefinition = processDefinition;
    this.deployment = deployment;
    
    addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    
    initImage();
  }
  
  protected void initImage() {
    processImageContainer = new VerticalLayout();
    
    Label processTitle = new Label(i18nManager.getMessage(Messages.PROCESS_HEADER_DIAGRAM));
    processTitle.addStyleName(ExplorerLayout.STYLE_H3);
    processImageContainer.addComponent(processTitle);
    
    if(processDefinition.getDiagramResourceName() != null) {
      StreamResource diagram = new ProcessDefinitionImageStreamResourceBuilder()
        .buildStreamResource(processDefinition, repositoryService);
      
      Embedded embedded = new Embedded(null, diagram);
      embedded.setType(Embedded.TYPE_IMAGE);
      embedded.setSizeUndefined();
      
      Panel imagePanel = new Panel(); // using panel for scrollbars
      imagePanel.addStyleName(Reindeer.PANEL_LIGHT);
      imagePanel.setWidth(100, UNITS_PERCENTAGE);
      imagePanel.setHeight(400, UNITS_PIXELS);
      HorizontalLayout panelLayout = new HorizontalLayout();
      panelLayout.setSizeUndefined();
      imagePanel.setContent(panelLayout);
      imagePanel.addComponent(embedded);
      
      processImageContainer.addComponent(imagePanel);
    } else {
      Label noImageAvailable = new Label(i18nManager.getMessage(Messages.PROCESS_NO_DIAGRAM));
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
