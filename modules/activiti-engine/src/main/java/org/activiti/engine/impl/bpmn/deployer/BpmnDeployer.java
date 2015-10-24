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

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class BpmnDeployer implements Deployer {

  private static final Logger log = LoggerFactory.getLogger(BpmnDeployer.class);

  protected IdGenerator idGenerator;
  protected ExpandedDeployment.BuilderFactory expandedDeploymentBuilderFactory;
  protected BpmnDeploymentUtilities deploymentUtility;
  protected CachingAndArtifactsManager cachingAndArtifactsManager;
  protected ProcessDefinitionDiagrammer processDefinitionDiagrammer;

  @Override
  public void deploy(DeploymentEntity deployment, Map<String, Object> deploymentSettings) {
    log.debug("Processing deployment {}", deployment.getName());

    // The ExpandedDeployment represents the deployment, the process definitions, and the BPMN 
    // resource, parse, and model associated with each process definition.
    ExpandedDeployment expandedDeployment = 
        expandedDeploymentBuilderFactory.getBuilderForDeploymentAndSettings(deployment, deploymentSettings)
        .build();
    
    deploymentUtility.verifyProcessDefinitionsDoNotShareKeys(expandedDeployment.getAllProcessDefinitions());

    deploymentUtility.copyDeploymentValuesToProcessDefinitions(
        expandedDeployment.getDeployment(), expandedDeployment.getAllProcessDefinitions());
    deploymentUtility.setResourceNamesOnProcessDefinitions(expandedDeployment);
    
    createAndPersistNewDiagramsAsNeeded(expandedDeployment);
    setProcessDefinitionDiagramNames(expandedDeployment);
    
    if (deployment.isNew()) {
      Map<ProcessDefinitionEntity, ProcessDefinitionEntity> mapOfNewProcessDefinitionToPreviousVersion =
          getPreviousVersionsOfProcessDefinitions(expandedDeployment);
      setProcessDefinitionVersionsAndIds(expandedDeployment, mapOfNewProcessDefinitionToPreviousVersion);
      persistProcessDefinitionsAndAuthorizations(expandedDeployment);

      updateTimersAndEvents(expandedDeployment, mapOfNewProcessDefinitionToPreviousVersion);
    } else {
      makeProcessDefinitionsConsistentWithPersistedVersions(expandedDeployment);
    }
    
    cachingAndArtifactsManager.updateCachingAndArtifacts(expandedDeployment);
  }

  /**
   * Creates new diagrams for process definitions if the deployment is new, the process definition in
   * question supports it, and the engine is configured to make new diagrams.  When this method
   * creates a new diagram, it also persists it via the ResourceEntityManager and adds it to the
   * resources of the deployment.
   */
  protected void createAndPersistNewDiagramsAsNeeded(ExpandedDeployment expandedDeployment) {
    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    DeploymentEntity deployment = expandedDeployment.getDeployment();
    
    if (expandedDeployment.getDeployment().isNew() && processEngineConfiguration.isCreateDiagramOnDeploy()) {
      ResourceEntityManager resourceEntityManager = processEngineConfiguration.getResourceEntityManager();
      
      for (ProcessDefinitionEntity processDefinition : expandedDeployment.getAllProcessDefinitions()) {
        if (processDefinitionDiagrammer.shouldCreateDiagram(processDefinition, deployment)) {
          ResourceEntity resource = processDefinitionDiagrammer.createDiagramForProcessDefinition(
              processDefinition, expandedDeployment.getBpmnParseForProcessDefinition(processDefinition));
          if (resource != null) {
            resourceEntityManager.insert(resource, false);
            deployment.addResource(resource);  // now we'll find it if we look for the diagram name later.
          }
        }
      }
    }
  }
  
  /**
   * Updates all the process definition entities to have the correct diagram resource name.  Must
   * be called after createAndPersistNewDiagramsAsNeeded to ensure that any newly-created diagrams
   * already have their resources attached to the deployment.
   */
  protected void setProcessDefinitionDiagramNames(ExpandedDeployment expandedDeployment) {
    Map<String, ResourceEntity> resources = expandedDeployment.getDeployment().getResources();

    for (ProcessDefinitionEntity processDefinition : expandedDeployment.getAllProcessDefinitions()) {
      String diagramResourceName = ResourceNameUtilities.getDiagramResourceName(processDefinition, resources);
      processDefinition.setDiagramResourceName(diagramResourceName);
    }
  }

  /**
   * Constructs a map from new ProcessDefinitionEntities to the previous version by key and tenant.
   * If no previous version exists, no map entry is created.
   */
  protected Map<ProcessDefinitionEntity, ProcessDefinitionEntity> getPreviousVersionsOfProcessDefinitions(
      ExpandedDeployment expandedDeployment) {
    Map<ProcessDefinitionEntity, ProcessDefinitionEntity> result = new LinkedHashMap<ProcessDefinitionEntity, ProcessDefinitionEntity>();
    
    for (ProcessDefinitionEntity newDefinition : expandedDeployment.getAllProcessDefinitions()) {
      ProcessDefinitionEntity existingDefinition = deploymentUtility.getMostRecentVersionOfProcessDefinition(newDefinition);
      
      if (existingDefinition != null) {
        result.put(newDefinition, existingDefinition);
      }
    }
    
    return result;
  }
  
  /**
   * Sets the version on each process definition entity, and the identifier.  If the map contains
   * an older version for a process definition, then the version is set to that older entity's
   * version plus one; otherwise it is set to 1.  Also dispatches an ENTITY_CREATED event.
   */
  protected void setProcessDefinitionVersionsAndIds(ExpandedDeployment expandedDeployment,
      Map<ProcessDefinitionEntity, ProcessDefinitionEntity> mapNewToOldProcessDefinitions) {
    CommandContext commandContext = Context.getCommandContext();

    for (ProcessDefinitionEntity processDefinition : expandedDeployment.getAllProcessDefinitions()) {
      int version = 1;
      
      ProcessDefinitionEntity latest = mapNewToOldProcessDefinitions.get(processDefinition);
      if (latest != null) {
        version = latest.getVersion() + 1;
      }
      
      processDefinition.setVersion(version);
      processDefinition.setId(getIdForNewProcessDefinition(processDefinition));
      
      if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, processDefinition));
      }
    }
  }
  
  /**
   * Saves each process definition.  It is assumed that the deployment is new, the definitions
   * have never been saved before, and that they have all their values properly set up.  Also
   * dispatches an ENTITY_INITIALIZED event.
   */
  protected void persistProcessDefinitionsAndAuthorizations(ExpandedDeployment expandedDeployment) {
    CommandContext commandContext = Context.getCommandContext();
    ProcessDefinitionEntityManager processDefinitionManager = commandContext.getProcessDefinitionEntityManager();
    
    for (ProcessDefinitionEntity processDefinition : expandedDeployment.getAllProcessDefinitions()) {
      processDefinitionManager.insert(processDefinition, false);
      
      deploymentUtility.addAuthorizationsForNewProcessDefinition(processDefinition);

      if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, processDefinition));
      }
    }
  }
  
  protected void updateTimersAndEvents(ExpandedDeployment expandedDeployment, 
      Map<ProcessDefinitionEntity, ProcessDefinitionEntity> mapNewToOldProcessDefinitions) {
    for (ProcessDefinitionEntity processDefinition : expandedDeployment.getAllProcessDefinitions()) {
      deploymentUtility.updateTimersAndEvents(processDefinition,
          mapNewToOldProcessDefinitions.get(processDefinition),
          expandedDeployment);
    }
  }
  
  /**
   * Returns the ID to use for a new process definition; subclasses may override this to provide
   * their own identification scheme.
   */
  protected String getIdForNewProcessDefinition(ProcessDefinitionEntity processDefinition) {
    String nextId = idGenerator.getNextId();
    
    String result = processDefinition.getKey() + ":" + processDefinition.getVersion() + ":" + nextId; // ACT-505
    // ACT-115: maximum id length is 64 characters
    if (result.length() > 64) {
      result = nextId;
    }
    
    return result;
  }
  
  /**
   * Loads the persisted version of each process definition and set values on the in-memory
   * version to be consistent.
   */
  protected void makeProcessDefinitionsConsistentWithPersistedVersions(ExpandedDeployment expandedDeployment) {
    for (ProcessDefinitionEntity processDefinition : expandedDeployment.getAllProcessDefinitions()) {
      ProcessDefinitionEntity persistedProcessDefinition = 
          deploymentUtility.getPersistedInstanceOfProcessDefinition(processDefinition);

      if (persistedProcessDefinition != null) {
        processDefinition.setId(persistedProcessDefinition.getId());
        processDefinition.setVersion(persistedProcessDefinition.getVersion());
        processDefinition.setSuspensionState(persistedProcessDefinition.getSuspensionState());
      }
    }
  }
  
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public ExpandedDeployment.BuilderFactory getExpandedDeploymentBuilderFactory() {
    return expandedDeploymentBuilderFactory;
  }
  
  public void setExpandedDeploymentBuilderFactory(ExpandedDeployment.BuilderFactory factory) {
    this.expandedDeploymentBuilderFactory = factory;
  }
  
  public BpmnDeploymentUtilities getBpmnDeploymentUtilities() {
    return deploymentUtility;
  }
  
  public void setBpmnDeploymentUtilities(BpmnDeploymentUtilities bpmnDeploymentUtilities) {
    this.deploymentUtility = bpmnDeploymentUtilities;
  }
  
  public CachingAndArtifactsManager getCachingAndArtifcatsManager() {
    return cachingAndArtifactsManager;
  }
  
  public void setCachingAndArtifactsManager(CachingAndArtifactsManager manager) {
    this.cachingAndArtifactsManager = manager;
  }
  
  public ProcessDefinitionDiagrammer getProcessDefinitionDiagrammer() {
    return processDefinitionDiagrammer;
  }
  
  public void setProcessDefinitionDiagrammer(ProcessDefinitionDiagrammer processDefinitionDiagrammer) {
    this.processDefinitionDiagrammer = processDefinitionDiagrammer;
  }
}
