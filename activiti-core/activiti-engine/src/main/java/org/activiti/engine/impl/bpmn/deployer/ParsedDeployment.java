/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.bpmn.deployer;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;

/**
 * An intermediate representation of a DeploymentEntity which keeps track of all of the entity's
 * ProcessDefinitionEntities and resources, and BPMN parses, models, and processes associated
 * with each ProcessDefinitionEntity - all produced by parsing the deployment.
 *
 * The ProcessDefinitionEntities are expected to be "not fully set-up" - they may be inconsistent with the
 * DeploymentEntity and/or the persisted versions, and if the deployment is new, they will not yet be persisted.
 */
public class ParsedDeployment {

  protected DeploymentEntity deploymentEntity;

  protected List<ProcessDefinitionEntity> processDefinitions;
  protected Map<ProcessDefinitionEntity, BpmnParse> mapProcessDefinitionsToParses;
  protected Map<ProcessDefinitionEntity, ResourceEntity> mapProcessDefinitionsToResources;

  public ParsedDeployment(
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

}
