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

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.cmd.DeploymentSettings;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An intermediate representation of a DeploymentEntity which keeps track of all of the entity's
 * ProcessDefinitionEntities and resources, and BPMN parses, models, and processes associated
 * with each ProcessDefinitionEntity.  The ProcessDefinitionEntities are expected to be
 * "not fully set-up" - they may be inconsistent with the DeploymentEntity and/or the persisted
 * versions, and if the deployment is new, they will not yet be persisted.
 */
public class ExpandedDeployment {
  private static final Logger log = LoggerFactory.getLogger(BpmnDeployer.class);

  private DeploymentEntity deploymentEntity;

  private List<ProcessDefinitionEntity> processDefinitions;
  private Map<ProcessDefinitionEntity, BpmnParse> mapProcessDefinitionsToParses;
  private Map<ProcessDefinitionEntity, ResourceEntity> mapProcessDefinitionsToResources;
  
  private ExpandedDeployment(
      DeploymentEntity entity, List<ProcessDefinitionEntity> processDefinitions,
      Map<ProcessDefinitionEntity, BpmnParse> mapProcessDefinitionsToParses,
      Map<ProcessDefinitionEntity, ResourceEntity> mapProcessDefinitionsToResources) {
    this.deploymentEntity = entity;
    this.processDefinitions = processDefinitions;
    this.mapProcessDefinitionsToParses = mapProcessDefinitionsToParses;
    this.mapProcessDefinitionsToResources = mapProcessDefinitionsToResources;
  }

  
  public DeploymentEntity getDeployment() {
    return deploymentEntity;
  }

  public List<ProcessDefinitionEntity> getAllProcessDefinitions() {
    return processDefinitions;
  }

  public ResourceEntity getResourceForProcessDefinition(ProcessDefinitionEntity processDefinition) {
    return mapProcessDefinitionsToResources.get(processDefinition);
  }

  public BpmnParse getBpmnParseForProcessDefinition(ProcessDefinitionEntity processDefinition) {
    return mapProcessDefinitionsToParses.get(processDefinition);
  }

  public BpmnModel getBpmnModelForProcessDefinition(ProcessDefinitionEntity processDefinition) {
    BpmnParse parse = getBpmnParseForProcessDefinition(processDefinition);
    
    return (parse == null ? null : parse.getBpmnModel());
  }
  
  public Process getProcessModelForProcessDefinition(ProcessDefinitionEntity processDefinition) {
    BpmnModel model = getBpmnModelForProcessDefinition(processDefinition);

    return (model == null ? null : model.getProcessById(processDefinition.getKey()));
  }
  
  public static class Builder {
    private final DeploymentEntity deployment;
    private final BpmnParser bpmnParser;
    private final Map<String, Object> deploymentSettings;

    public Builder(DeploymentEntity deployment, BpmnParser bpmnParser, Map<String, Object> deploymentSettings) {
      this.deployment = deployment;
      this.bpmnParser = bpmnParser;
      this.deploymentSettings = deploymentSettings;
    }

    public ExpandedDeployment build() {
      List<ProcessDefinitionEntity> processDefinitions = new ArrayList<ProcessDefinitionEntity>();
      Map<ProcessDefinitionEntity, BpmnParse> mapProcessDefinitionsToParses = new LinkedHashMap<ProcessDefinitionEntity, BpmnParse>();
      Map<ProcessDefinitionEntity, ResourceEntity> mapProcessDefinitionsToResources = new LinkedHashMap<ProcessDefinitionEntity, ResourceEntity>();

      for (ResourceEntity resource : deployment.getResources().values()) {
        if (isBpmnResource(resource.getName())) {
          log.debug("Processing BPMN resource {}", resource.getName());
          BpmnParse parse = createExecutedBpmnParseFromResource(resource);
          for (ProcessDefinitionEntity oneDefinition : parse.getProcessDefinitions()) {
            processDefinitions.add(oneDefinition);
            mapProcessDefinitionsToParses.put(oneDefinition, parse);
            mapProcessDefinitionsToResources.put(oneDefinition, resource);
          }
        }
      }

      return new ExpandedDeployment(deployment,processDefinitions, mapProcessDefinitionsToParses, mapProcessDefinitionsToResources);
    }
    
    private BpmnParse createExecutedBpmnParseFromResource(ResourceEntity resource) {
      String resourceName = resource.getName();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(resource.getBytes());

      BpmnParse bpmnParse = bpmnParser.createParse()
          .sourceInputStream(inputStream)
          .setSourceSystemId(resourceName)
          .deployment(deployment)
          .name(resourceName);

      if (deploymentSettings != null) {

        // Schema validation if needed
        if (deploymentSettings.containsKey(DeploymentSettings.IS_BPMN20_XSD_VALIDATION_ENABLED)) {
          bpmnParse.setValidateSchema((Boolean) deploymentSettings.get(DeploymentSettings.IS_BPMN20_XSD_VALIDATION_ENABLED));
        }

        // Process validation if needed
        if (deploymentSettings.containsKey(DeploymentSettings.IS_PROCESS_VALIDATION_ENABLED)) {
          bpmnParse.setValidateProcess((Boolean) deploymentSettings.get(DeploymentSettings.IS_PROCESS_VALIDATION_ENABLED));
        }

      } else {
        // On redeploy, we assume it is validated at the first
        // deploy
        bpmnParse.setValidateSchema(false);
        bpmnParse.setValidateProcess(false);
      }
      bpmnParse.execute();
      return bpmnParse;
    }
  }
  
  public static class BuilderFactory {
    protected BpmnParser bpmnParser;
    
    public BpmnParser getBpmnParser() {
      return bpmnParser;
    }
    
    public void setBpmnParser(BpmnParser bpmnParser) {
      this.bpmnParser = bpmnParser;
    }
    
    public Builder getBuilderForDeployment(DeploymentEntity deployment) {
      return getBuilderForDeploymentAndSettings(deployment, null);
    }
    
    public Builder getBuilderForDeploymentAndSettings(DeploymentEntity deployment, Map<String, Object> deploymentSettings) {
      return new Builder(deployment, bpmnParser, deploymentSettings);
    }
  }
    
  private static boolean isBpmnResource(String resourceName) {
    for (String suffix : ResourceNameUtilities.BPMN_RESOURCE_SUFFIXES) {
      if (resourceName.endsWith(suffix)) {
        return true;
      }
    }
     
    return false;
  }
}

