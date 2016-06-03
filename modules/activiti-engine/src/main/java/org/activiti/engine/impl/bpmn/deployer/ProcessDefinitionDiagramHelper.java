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
package org.activiti.engine.impl.bpmn.deployer;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates diagrams from process definitions.
 */
public class ProcessDefinitionDiagramHelper {
  
  private static final Logger log = LoggerFactory.getLogger(ProcessDefinitionDiagramHelper.class);
  
  /**
   * Generates a diagram resource for a ProcessDefinitionEntity and associated BpmnParse.  The
   * returned resource has not yet been persisted, nor attached to the ProcessDefinitionEntity.
   * This requires that the ProcessDefinitionEntity have its key and resource name already set.
   * 
   * The caller must determine whether creating a diagram for this process definition is appropriate or not,
   * for example see {@link #shouldCreateDiagram(ProcessDefinitionEntity, DeploymentEntity)}.
   */
  public ResourceEntity createDiagramForProcessDefinition(ProcessDefinitionEntity processDefinition, BpmnParse bpmnParse) {
    
    if (StringUtils.isEmpty(processDefinition.getKey()) || StringUtils.isEmpty(processDefinition.getResourceName())) {
      throw new IllegalStateException("Provided process definition must have both key and resource name set.");
    }
    
    ResourceEntity resource = createResourceEntity();
    ProcessEngineConfiguration processEngineConfiguration = Context.getCommandContext().getProcessEngineConfiguration();
    try {
      byte[] diagramBytes = IoUtil.readInputStream(
          processEngineConfiguration.getProcessDiagramGenerator().generateDiagram(bpmnParse.getBpmnModel(), "png",
              processEngineConfiguration.getActivityFontName(),
              processEngineConfiguration.getLabelFontName(),
              processEngineConfiguration.getAnnotationFontName(),
              processEngineConfiguration.getClassLoader()), null);
        String diagramResourceName = ResourceNameUtil.getProcessDiagramResourceName(
            processDefinition.getResourceName(), processDefinition.getKey(), "png");
        
        resource.setName(diagramResourceName);
        resource.setBytes(diagramBytes);
        resource.setDeploymentId(processDefinition.getDeploymentId());

        // Mark the resource as 'generated'
        resource.setGenerated(true);
        
    } catch (Throwable t) { // if anything goes wrong, we don't store the image (the process will still be executable).
      log.warn("Error while generating process diagram, image will not be stored in repository", t);
      resource = null;
    }
    
    return resource;
  }
  
  protected ResourceEntity createResourceEntity() {
    return Context.getCommandContext().getProcessEngineConfiguration().getResourceEntityManager().create();
  }
  
  public boolean shouldCreateDiagram(ProcessDefinitionEntity processDefinition, DeploymentEntity deployment) {
    if (deployment.isNew() 
        && processDefinition.isGraphicalNotationDefined()
        && Context.getCommandContext().getProcessEngineConfiguration().isCreateDiagramOnDeploy()) {
      
      // If the 'getProcessDiagramResourceNameFromDeployment' call returns null, it means 
      // no diagram image for the process definition was provided in the deployment resources.
      return ResourceNameUtil.getProcessDiagramResourceNameFromDeployment(processDefinition, deployment.getResources()) == null;
    }
    
    return false;
  }
}

