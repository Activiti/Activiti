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
import java.util.Collections;
import java.util.UUID;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.ui.util.InputStreamStreamSource;
import org.activiti.image.ProcessDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;


/**
 * Builder that is capable of creating a {@link StreamResource} for a given
 * process-definition, containing the diagram image, if available.
 * 
 * @author Frederik Heremans
 */
public class ProcessDefinitionImageStreamResourceBuilder {
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessDefinitionImageStreamResourceBuilder.class);
  
  public StreamResource buildStreamResource(ProcessDefinition processDefinition, RepositoryService repositoryService) {
    
    StreamResource imageResource = null;
    
    if(processDefinition.getDiagramResourceName() != null) {
      final InputStream definitionImageStream = repositoryService.getResourceAsStream(
        processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName());
      
      StreamSource streamSource = new InputStreamStreamSource(definitionImageStream);
      
      // Creating image name based on process-definition ID is fine, since the diagram image cannot
      // be altered once deployed.
      String imageExtension = extractImageExtension(processDefinition.getDiagramResourceName());
      String fileName = processDefinition.getId() + "." + imageExtension;
      
      imageResource = new StreamResource(streamSource, fileName, ExplorerApp.get());
    }
    
    return imageResource;
  }

  public StreamResource buildStreamResource(ProcessInstance processInstance, RepositoryService repositoryService, 
      RuntimeService runtimeService, ProcessDiagramGenerator diagramGenerator, ProcessEngineConfiguration processEngineConfig) {

    StreamResource imageResource = null;
    
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processInstance
            .getProcessDefinitionId());

    if (processDefinition != null && processDefinition.isGraphicalNotationDefined()) {
      try {
        
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        InputStream definitionImageStream = diagramGenerator.generateDiagram(bpmnModel, "png",
          runtimeService.getActiveActivityIds(processInstance.getId()), Collections.<String>emptyList(), 
          processEngineConfig.getActivityFontName(), processEngineConfig.getLabelFontName(), processEngineConfig.getAnnotationFontName(),  
          processEngineConfig.getClassLoader(), 1.0);
              
        if(definitionImageStream != null) {
          StreamSource streamSource = new InputStreamStreamSource(definitionImageStream);
          
          // Create image name
          String imageExtension = extractImageExtension(processDefinition.getDiagramResourceName());
          String fileName = processInstance.getId() + UUID.randomUUID() + "." + imageExtension;
          
          imageResource = new StreamResource(streamSource, fileName, ExplorerApp.get()); 
        }
      } catch(Throwable t) {
        // Image can't be generated, ignore this
        LOGGER.warn("Process image cannot be generated due to exception: {} - {}", t.getClass().getName(), t.getMessage());
      }
    }
    return imageResource;
  }
  
  public StreamResource buildStreamResource(String processInstanceId, String processDefinitionId, 
      RepositoryService repositoryService, RuntimeService runtimeService, ProcessDiagramGenerator diagramGenerator,
      ProcessEngineConfiguration processEngineConfig) {

    StreamResource imageResource = null;
    
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinitionId);

    if (processDefinition != null && processDefinition.isGraphicalNotationDefined()) {
      
      BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
      InputStream definitionImageStream = diagramGenerator.generateDiagram(bpmnModel, "png",
        runtimeService.getActiveActivityIds(processInstanceId), Collections.<String>emptyList(), 
        processEngineConfig.getActivityFontName(), processEngineConfig.getLabelFontName(), processEngineConfig.getAnnotationFontName(),  
        processEngineConfig.getClassLoader(), 1.0);
      
      StreamSource streamSource = new InputStreamStreamSource(definitionImageStream);
      
      // Create image name
      String imageExtension = extractImageExtension(processDefinition.getDiagramResourceName());
      String fileName = processInstanceId + UUID.randomUUID() + "." + imageExtension;
      
      imageResource = new StreamResource(streamSource, fileName, ExplorerApp.get()); 
    }
    return imageResource;
  }

  protected String extractImageExtension(String diagramResourceName) {
    String[] parts = diagramResourceName.split(".");
    if(parts.length > 1) {
      return parts[parts.length - 1];
    }
    return Constants.DEFAULT_DIAGRAM_IMAGE_EXTENSION;
  }
}