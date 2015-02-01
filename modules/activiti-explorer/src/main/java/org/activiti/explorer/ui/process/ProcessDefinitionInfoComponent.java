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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.jobexecutor.TimerActivateProcessDefinitionHandler;
import org.activiti.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.util.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ExternalResource;
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
 * @author Joram Barrez
 */
public class ProcessDefinitionInfoComponent extends VerticalLayout {

  private static final long serialVersionUID = 1L;
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessDefinitionInfoComponent.class);

  // Services
  protected transient RepositoryService repositoryService;
  protected transient ManagementService managementService;
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
    this.managementService = ProcessEngines.getDefaultProcessEngine().getManagementService();
    this.i18nManager = ExplorerApp.get().getI18nManager(); 
    
    this.processDefinition = processDefinition;
    this.deployment = deployment;
    
    addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    
    initSuspensionStateInformation();
    initImage();
  }
  
  protected void initSuspensionStateInformation() {
    List<Job> jobs = managementService.createJobQuery()
            .processDefinitionId(processDefinition.getId()).orderByJobDuedate().asc().list();
    List<JobEntity> suspensionStateJobs = new ArrayList<JobEntity>();
    
    // TODO: this is a hack (ie the cast to JobEntity)... we must clean this in the engine!
    for (Job job : jobs) {
      JobEntity jobEntity = (JobEntity) job;
      if (jobEntity.getJobHandlerType().equals(TimerSuspendProcessDefinitionHandler.TYPE) 
              || jobEntity.getJobHandlerType().equals(TimerActivateProcessDefinitionHandler.TYPE)) {
        suspensionStateJobs.add(jobEntity);
      }
    }
    
    if (!suspensionStateJobs.isEmpty()) {
      
      // Header
      Label suspensionStateTitle = new Label(i18nManager.getMessage(Messages.PROCESS_HEADER_SUSPENSION_STATE));
      suspensionStateTitle.addStyleName(ExplorerLayout.STYLE_H3);
      addComponent(suspensionStateTitle);
      addEmptySpace(this);
      
      // Actual suspend/activation jobs
      for (JobEntity jobEntity : suspensionStateJobs) {
        if (jobEntity.getJobHandlerType().equals(TimerSuspendProcessDefinitionHandler.TYPE))  {
          Label suspendLabel = new Label(i18nManager.getMessage(Messages.PROCESS_SCHEDULED_SUSPEND, 
                  Constants.DEFAULT_TIME_FORMATTER.format(jobEntity.getDuedate())), Label.CONTENT_XHTML);
          addComponent(suspendLabel);
        } else if (jobEntity.getJobHandlerType().equals(TimerActivateProcessDefinitionHandler.TYPE))  {
          Label suspendLabel = new Label(i18nManager.getMessage(Messages.PROCESS_SCHEDULED_ACTIVATE, 
                  Constants.DEFAULT_TIME_FORMATTER.format(jobEntity.getDuedate())), Label.CONTENT_XHTML);
          addComponent(suspendLabel);
        }
      }
    }
    
    addEmptySpace(this);
  }
  
  protected void initImage() {
    processImageContainer = new VerticalLayout();
    
    Label processTitle = new Label(i18nManager.getMessage(Messages.PROCESS_HEADER_DIAGRAM));
    processTitle.addStyleName(ExplorerLayout.STYLE_H3);
    processImageContainer.addComponent(processTitle);

    boolean didDrawImage = false;
    
    if (ExplorerApp.get().isUseJavascriptDiagram()) {
      try {
        
        final InputStream definitionStream = repositoryService.getResourceAsStream(
            processDefinition.getDeploymentId(), processDefinition.getResourceName());
        XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
        XMLStreamReader xtr = xif.createXMLStreamReader(definitionStream);
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
        
        if (!bpmnModel.getFlowLocationMap().isEmpty()) {
          
          int maxX = 0;
          int maxY = 0;
          for (String key : bpmnModel.getLocationMap().keySet()) {
            GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(key);
            double elementX = graphicInfo.getX() + graphicInfo.getWidth();
            if (maxX < elementX) {
              maxX = (int) elementX;
            }
            double elementY = graphicInfo.getY() + graphicInfo.getHeight();
            if (maxY < elementY) {
              maxY = (int) elementY;
            }
          }
          
          Panel imagePanel = new Panel(); // using panel for scrollbars
          imagePanel.addStyleName(Reindeer.PANEL_LIGHT);
          imagePanel.setWidth(100, UNITS_PERCENTAGE);
          imagePanel.setHeight(100, UNITS_PERCENTAGE);

	      URL explorerURL = ExplorerApp.get().getURL();
	      URL url = new URL(explorerURL.getProtocol(), explorerURL.getHost(), explorerURL.getPort(), explorerURL.getPath().replace("/ui", "") +
              "diagram-viewer/index.html?processDefinitionId=" + processDefinition.getId());
          Embedded browserPanel = new Embedded("", new ExternalResource(url));
          browserPanel.setType(Embedded.TYPE_BROWSER);
          browserPanel.setWidth(maxX + 350 + "px");
          browserPanel.setHeight(maxY + 220 + "px");
          
          HorizontalLayout panelLayout = new HorizontalLayout();
          panelLayout.setSizeUndefined();
          imagePanel.setContent(panelLayout);
          imagePanel.addComponent(browserPanel);
          
          processImageContainer.addComponent(imagePanel);
          
          didDrawImage = true;
        }
        
      } catch (Exception e) {
        LOGGER.error("Error loading process diagram component", e);
      }
    }
    
    if(didDrawImage == false) {
      
      StreamResource diagram = null;
      
      // Try generating process-image stream
      if(processDefinition.getDiagramResourceName() != null) {
         diagram = new ProcessDefinitionImageStreamResourceBuilder()
          .buildStreamResource(processDefinition, repositoryService);
      }
      
      if (diagram != null) {
      
        Embedded embedded = new Embedded(null, diagram);
        embedded.setType(Embedded.TYPE_IMAGE);
        embedded.setSizeUndefined();
        
        Panel imagePanel = new Panel(); // using panel for scrollbars
        imagePanel.addStyleName(Reindeer.PANEL_LIGHT);
        imagePanel.setWidth(100, UNITS_PERCENTAGE);
        imagePanel.setHeight(100, UNITS_PERCENTAGE);
        HorizontalLayout panelLayout = new HorizontalLayout();
        panelLayout.setSizeUndefined();
        imagePanel.setContent(panelLayout);
        imagePanel.addComponent(embedded);
        processImageContainer.addComponent(imagePanel);
        
        didDrawImage = true;
      }
    } 
    
    if (didDrawImage == false) {
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
