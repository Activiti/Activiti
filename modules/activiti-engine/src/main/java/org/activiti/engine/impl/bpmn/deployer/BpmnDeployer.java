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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.CancelJobsCmd;
import org.activiti.engine.impl.cmd.DeploymentSettings;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.event.MessageEventHandler;
import org.activiti.engine.impl.event.SignalEventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionInfoCacheObject;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntityManager;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.IdentityLinkType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class BpmnDeployer implements Deployer {

  private static final Logger log = LoggerFactory.getLogger(BpmnDeployer.class);

  public static final String[] BPMN_RESOURCE_SUFFIXES = new String[] { "bpmn20.xml", "bpmn" };
  public static final String[] DIAGRAM_SUFFIXES = new String[]{"png", "jpg", "gif", "svg"};

  protected ExpressionManager expressionManager;
  protected BpmnParser bpmnParser;
  protected IdGenerator idGenerator;

  public void deploy(DeploymentEntity deployment, Map<String, Object> deploymentSettings) {
    log.debug("Processing deployment {}", deployment.getName());
    
    List<ProcessDefinitionEntity> processDefinitions = new ArrayList<ProcessDefinitionEntity>();
    Map<String, ResourceEntity> resources = deployment.getResources();
    Map<String, BpmnModel> bpmnModelMap = new HashMap<String, BpmnModel>();

    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    for (String resourceName : resources.keySet()) {

      log.info("Processing resource {}", resourceName);
      if (isBpmnResource(resourceName)) {
        ResourceEntity resource = resources.get(resourceName);
        byte[] bytes = resource.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        
        BpmnParse bpmnParse = bpmnParser
          .createParse()
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
        	// On redeploy, we assume it is validated at the first deploy
        	bpmnParse.setValidateSchema(false);
        	bpmnParse.setValidateProcess(false);
        }
        
        bpmnParse.execute();
        
        for (ProcessDefinitionEntity processDefinition: bpmnParse.getProcessDefinitions()) {
          processDefinition.setResourceName(resourceName);
          
          if (deployment.getTenantId() != null) {
          	processDefinition.setTenantId(deployment.getTenantId()); // process definition inherits the tenant id
          }
          
          String diagramResourceName = getDiagramResourceForProcess(resourceName, processDefinition.getKey(), resources);
                   
          // Only generate the resource when deployment is new to prevent modification of deployment resources 
          // after the process-definition is actually deployed. Also to prevent resource-generation failure every
          // time the process definition is added to the deployment-cache when diagram-generation has failed the first time.
          if(deployment.isNew()) {
            if (processEngineConfiguration.isCreateDiagramOnDeploy() &&
                  diagramResourceName==null && processDefinition.isGraphicalNotationDefined()) {
              try {
                  byte[] diagramBytes = IoUtil.readInputStream(processEngineConfiguration.
                    getProcessDiagramGenerator().generateDiagram(bpmnParse.getBpmnModel(), "png", processEngineConfiguration.getActivityFontName(),
                        processEngineConfiguration.getLabelFontName(),processEngineConfiguration.getAnnotationFontName(), processEngineConfiguration.getClassLoader()), null);
                  diagramResourceName = getProcessImageResourceName(resourceName, processDefinition.getKey(), "png");
                  createResource(diagramResourceName, diagramBytes, deployment);
              } catch (Throwable t) { // if anything goes wrong, we don't store the image (the process will still be executable).
                log.warn("Error while generating process diagram, image will not be stored in repository", t);
              }
            } 
          }
          
          processDefinition.setDiagramResourceName(diagramResourceName);
          processDefinitions.add(processDefinition);
          bpmnModelMap.put(processDefinition.getKey(), bpmnParse.getBpmnModel());
        }
      }
    }
    
    // check if there are process definitions with the same process key to prevent database unique index violation
    List<String> keyList = new ArrayList<String>();
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      if (keyList.contains(processDefinition.getKey())) {
        throw new ActivitiException("The deployment contains process definitions with the same key '"+ processDefinition.getKey() +"' (process id atrribute), this is not allowed");
      }
      keyList.add(processDefinition.getKey());
    }
    
    CommandContext commandContext = Context.getCommandContext();
    ProcessDefinitionEntityManager processDefinitionManager = commandContext.getProcessDefinitionEntityManager();
    DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      List<TimerEntity> timers = new ArrayList<TimerEntity>();
      if (deployment.isNew()) {
        int processDefinitionVersion;

        ProcessDefinitionEntity latestProcessDefinition = null;
        if (processDefinition.getTenantId() != null && !ProcessEngineConfiguration.NO_TENANT_ID.equals(processDefinition.getTenantId())) {
        	latestProcessDefinition = processDefinitionManager
        			.findLatestProcessDefinitionByKeyAndTenantId(processDefinition.getKey(), processDefinition.getTenantId());
        } else {
        	latestProcessDefinition = processDefinitionManager
        			.findLatestProcessDefinitionByKey(processDefinition.getKey());
        }
        		
        if (latestProcessDefinition != null) {
          processDefinitionVersion = latestProcessDefinition.getVersion() + 1;
        } else {
          processDefinitionVersion = 1;
        }

        processDefinition.setVersion(processDefinitionVersion);
        processDefinition.setDeploymentId(deployment.getId());

        String nextId = idGenerator.getNextId();
        String processDefinitionId = processDefinition.getKey() 
          + ":" + processDefinition.getVersion()
          + ":" + nextId; // ACT-505
                   
        // ACT-115: maximum id length is 64 charcaters
        if (processDefinitionId.length() > 64) {          
          processDefinitionId = nextId; 
        }
        processDefinition.setId(processDefinitionId);
        
        if(commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        	commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
        			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, processDefinition));
        }

        removeObsoleteTimers(processDefinition);
        addTimerDeclarations(processDefinition, timers);
        
        removeExistingMessageEventSubscriptions(processDefinition, latestProcessDefinition);
        addMessageEventSubscriptions(processDefinition);
        
        removeExistingSignalEventSubScription(processDefinition, latestProcessDefinition);
        addSignalEventSubscriptions(processDefinition);

        dbSqlSession.insert(processDefinition);
        addAuthorizations(processDefinition);

        if(commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        	commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
        			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, processDefinition));
        }

        scheduleTimers(timers);

      } else {
        String deploymentId = deployment.getId();
        processDefinition.setDeploymentId(deploymentId);
        
        ProcessDefinitionEntity persistedProcessDefinition = null; 
        if (processDefinition.getTenantId() == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(processDefinition.getTenantId())) {
        	persistedProcessDefinition = processDefinitionManager.findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinition.getKey());
        } else {
        	persistedProcessDefinition = processDefinitionManager.findProcessDefinitionByDeploymentAndKeyAndTenantId(deploymentId, processDefinition.getKey(), processDefinition.getTenantId());
        }
        
        if (persistedProcessDefinition != null) {
        	processDefinition.setId(persistedProcessDefinition.getId());
        	processDefinition.setVersion(persistedProcessDefinition.getVersion());
        	processDefinition.setSuspensionState(persistedProcessDefinition.getSuspensionState());
        }
      }

      // Add to cache
      DeploymentManager deploymentManager = processEngineConfiguration.getDeploymentManager();
      deploymentManager.getProcessDefinitionCache().add(processDefinition.getId(), processDefinition);
      addDefinitionInfoToCache(processDefinition, processEngineConfiguration, commandContext);
      
      // Add to deployment for further usage
      deployment.addDeployedArtifact(processDefinition);
      
      createLocalizationValues(processDefinition.getId(), bpmnModelMap.get(processDefinition.getKey()).getProcessById(processDefinition.getKey()));
    }
  }
  
  protected void addDefinitionInfoToCache(ProcessDefinitionEntity processDefinition, 
      ProcessEngineConfigurationImpl processEngineConfiguration, CommandContext commandContext) {
    
    if (processEngineConfiguration.isEnableProcessDefinitionInfoCache() == false) {
      return;
    }
    
    DeploymentManager deploymentManager = processEngineConfiguration.getDeploymentManager();
    ProcessDefinitionInfoEntityManager definitionInfoEntityManager = commandContext.getProcessDefinitionInfoEntityManager();
    ObjectMapper objectMapper = commandContext.getProcessEngineConfiguration().getObjectMapper();
    ProcessDefinitionInfoEntity definitionInfoEntity = definitionInfoEntityManager.findProcessDefinitionInfoByProcessDefinitionId(processDefinition.getId());
    
    ObjectNode infoNode = null;
    if (definitionInfoEntity != null && definitionInfoEntity.getInfoJsonId() != null) {
      byte[] infoBytes = definitionInfoEntityManager.findInfoJsonById(definitionInfoEntity.getInfoJsonId());
      if (infoBytes != null) {
        try {
          infoNode = (ObjectNode) objectMapper.readTree(infoBytes);
        } catch (Exception e) {
          throw new ActivitiException("Error deserializing json info for process definition " + processDefinition.getId());
        }
      }
    }
    
    ProcessDefinitionInfoCacheObject definitionCacheObject = new ProcessDefinitionInfoCacheObject();
    if (definitionInfoEntity == null) {
      definitionCacheObject.setRevision(0);
    } else {
      definitionCacheObject.setId(definitionInfoEntity.getId());
      definitionCacheObject.setRevision(definitionInfoEntity.getRevision());
    }
    
    if (infoNode == null) {
      infoNode = objectMapper.createObjectNode();
    }
    definitionCacheObject.setInfoNode(infoNode);
    
    deploymentManager.getProcessDefinitionInfoCache().add(processDefinition.getId(), definitionCacheObject);
  }

  protected void scheduleTimers(List<TimerEntity> timers) {
    for (TimerEntity timer : timers) {
      Context
        .getCommandContext()
        .getJobEntityManager()
        .schedule(timer);
    }
  }

  @SuppressWarnings("unchecked")
  protected void addTimerDeclarations(ProcessDefinitionEntity processDefinition, List<TimerEntity> timers) {
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(BpmnParse.PROPERTYNAME_START_TIMER);
    if (timerDeclarations!=null) {
      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
        TimerEntity timer = timerDeclaration.prepareTimerEntity(null);
        if (timer!=null) {
          timer.setProcessDefinitionId(processDefinition.getId());
	        
          // Inherit timer (if appliccable)
          if (processDefinition.getTenantId() != null) {
            timer.setTenantId(processDefinition.getTenantId());
          }
          timers.add(timer);
        }
      }
    }
  }

  protected void removeObsoleteTimers(ProcessDefinitionEntity processDefinition) {
  	
  	List<Job> jobsToDelete = null;
  	
  	if (processDefinition.getTenantId() != null && !ProcessEngineConfiguration.NO_TENANT_ID.equals(processDefinition.getTenantId())) {
  		jobsToDelete = Context.getCommandContext().getJobEntityManager().findJobsByTypeAndProcessDefinitionKeyAndTenantId(
  				TimerStartEventJobHandler.TYPE, processDefinition.getKey(), processDefinition.getTenantId());
    } else {
    	jobsToDelete = Context.getCommandContext().getJobEntityManager()
    			.findJobsByTypeAndProcessDefinitionKeyNoTenantId(TimerStartEventJobHandler.TYPE, processDefinition.getKey());
    }

  	if (jobsToDelete != null) {
	    for (Job job :jobsToDelete) {
	        new CancelJobsCmd(job.getId()).execute(Context.getCommandContext());
	    }
  	}
  }
  
  protected void removeExistingMessageEventSubscriptions(ProcessDefinitionEntity processDefinition, ProcessDefinitionEntity latestProcessDefinition) {
    if(latestProcessDefinition != null) {
      CommandContext commandContext = Context.getCommandContext();
      
      List<EventSubscriptionEntity> subscriptionsToDisable = commandContext
        .getEventSubscriptionEntityManager()
        .findEventSubscriptionsByTypeAndProcessDefinitionId(MessageEventHandler.EVENT_HANDLER_TYPE, latestProcessDefinition.getId(), latestProcessDefinition.getTenantId());
      
      for (EventSubscriptionEntity eventSubscriptionEntity : subscriptionsToDisable) {
        eventSubscriptionEntity.delete();        
      } 
      
    }
  }
  
  @SuppressWarnings("unchecked")
  protected void addMessageEventSubscriptions(ProcessDefinitionEntity processDefinition) {
    CommandContext commandContext = Context.getCommandContext();
    List<EventSubscriptionDeclaration> eventDefinitions = (List<EventSubscriptionDeclaration>) processDefinition.getProperty(BpmnParse.PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION);
    if(eventDefinitions != null) {  
      
      Set<String> messageNames = new HashSet<String>();
      for (EventSubscriptionDeclaration eventDefinition : eventDefinitions) {
        if(eventDefinition.getEventType().equals("message") && eventDefinition.isStartEvent()) {
          
          if (!messageNames.contains(eventDefinition.getEventName())) {
            messageNames.add(eventDefinition.getEventName());
          } else {
            throw new ActivitiException("Cannot deploy process definition '" + processDefinition.getResourceName()
                + "': there are multiple message event subscriptions for the message with name '" + eventDefinition.getEventName() + "'.");
          }
          
          // look for subscriptions for the same name in db:
          List<EventSubscriptionEntity> subscriptionsForSameMessageName = commandContext
            .getEventSubscriptionEntityManager()
            .findEventSubscriptionsByName(MessageEventHandler.EVENT_HANDLER_TYPE, 
            		eventDefinition.getEventName(), processDefinition.getTenantId());
          
          // also look for subscriptions created in the session:
          List<MessageEventSubscriptionEntity> cachedSubscriptions = commandContext
            .getDbSqlSession()
            .findInCache(MessageEventSubscriptionEntity.class);
          for (MessageEventSubscriptionEntity cachedSubscription : cachedSubscriptions) {
            if(eventDefinition.getEventName().equals(cachedSubscription.getEventName())
                    && !subscriptionsForSameMessageName.contains(cachedSubscription)) {
              subscriptionsForSameMessageName.add(cachedSubscription);
            }
          }
          
          // remove subscriptions deleted in the same command
          subscriptionsForSameMessageName = commandContext
                  .getDbSqlSession()
                  .pruneDeletedEntities(subscriptionsForSameMessageName);
                
          for (EventSubscriptionEntity eventSubscriptionEntity : subscriptionsForSameMessageName) {
            // throw exception only if there's already a subscription as start event
            
            // no process instance-id = it's a message start event
            if(StringUtils.isEmpty(eventSubscriptionEntity.getProcessInstanceId())) {
              throw new ActivitiException("Cannot deploy process definition '" + processDefinition.getResourceName()
                      + "': there already is a message event subscription for the message with name '" + eventDefinition.getEventName() + "'.");
            }
          }
          
          MessageEventSubscriptionEntity newSubscription = new MessageEventSubscriptionEntity();
          newSubscription.setEventName(eventDefinition.getEventName());
          newSubscription.setActivityId(eventDefinition.getActivityId());
          newSubscription.setConfiguration(processDefinition.getId());
          newSubscription.setProcessDefinitionId(processDefinition.getId());

          if (processDefinition.getTenantId() != null) {
          	newSubscription.setTenantId(processDefinition.getTenantId());
          }
          
          newSubscription.insert();
        }
      }
    }      
  }
  
  protected void removeExistingSignalEventSubScription(ProcessDefinitionEntity processDefinition, ProcessDefinitionEntity latestProcessDefinition) {
    if(latestProcessDefinition != null) {
      CommandContext commandContext = Context.getCommandContext();
      
      List<EventSubscriptionEntity> subscriptionsToDisable = commandContext
        .getEventSubscriptionEntityManager()
        .findEventSubscriptionsByTypeAndProcessDefinitionId(SignalEventHandler.EVENT_HANDLER_TYPE, latestProcessDefinition.getId(), latestProcessDefinition.getTenantId());
      
      for (EventSubscriptionEntity eventSubscriptionEntity : subscriptionsToDisable) {
        eventSubscriptionEntity.delete();  
      } 
      
    }
  }
  
  @SuppressWarnings("unchecked")
  protected void addSignalEventSubscriptions(ProcessDefinitionEntity processDefinition) {
     List<EventSubscriptionDeclaration> eventDefinitions = (List<EventSubscriptionDeclaration>) processDefinition.getProperty(BpmnParse.PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION);
     if(eventDefinitions != null) {     
       for (EventSubscriptionDeclaration eventDefinition : eventDefinitions) {
         if(eventDefinition.getEventType().equals("signal") && eventDefinition.isStartEvent()) {
        	 
        	 SignalEventSubscriptionEntity subscriptionEntity = new SignalEventSubscriptionEntity();
        	 subscriptionEntity.setEventName(eventDefinition.getEventName());
        	 subscriptionEntity.setActivityId(eventDefinition.getActivityId());
        	 subscriptionEntity.setProcessDefinitionId(processDefinition.getId());
        	 if (processDefinition.getTenantId() != null) {
        		 subscriptionEntity.setTenantId(processDefinition.getTenantId());
           }
        	 subscriptionEntity.insert();
        	 
         }	 
       }
     }      
  }
  
  protected void createLocalizationValues(String processDefinitionId, Process process) {
    if (process == null) return;
    
    CommandContext commandContext = Context.getCommandContext();
    DynamicBpmnService dynamicBpmnService = commandContext.getProcessEngineConfiguration().getDynamicBpmnService();
    ObjectNode infoNode = dynamicBpmnService.getProcessDefinitionInfo(processDefinitionId);

    boolean localizationValuesChanged = false;
    List<ExtensionElement> localizationElements = process.getExtensionElements().get("localization");
    if (localizationElements != null) {
      for (ExtensionElement localizationElement : localizationElements) {
        if (BpmnXMLConstants.ACTIVITI_EXTENSIONS_PREFIX.equals(localizationElement.getNamespacePrefix())) {
          String locale = localizationElement.getAttributeValue(null, "locale");
          String name = localizationElement.getAttributeValue(null, "name");
          String documentation = null;
          List<ExtensionElement> documentationElements = localizationElement.getChildElements().get("documentation");
          if (documentationElements != null) {
            for (ExtensionElement documentationElement : documentationElements) {
              documentation = StringUtils.trimToNull(documentationElement.getElementText());
              break;
            }
          }

          String processId = process.getId();
          if (isEqualToCurrentLocalizationValue(locale, processId, "name", name, infoNode) == false) {
            dynamicBpmnService.changeLocalizationName(locale, processId, name, infoNode);
            localizationValuesChanged = true;
          }
          
          if (documentation != null && isEqualToCurrentLocalizationValue(locale, processId, "description", documentation, infoNode) == false) {
            dynamicBpmnService.changeLocalizationDescription(locale, processId, documentation, infoNode);
            localizationValuesChanged = true;
          }
          
          break;
        }
      }
    }

    boolean isFlowElementLocalizationChanged = localizeFlowElements(process.getFlowElements(), infoNode);
    boolean isDataObjectLocalizationChanged = localizeDataObjectElements(process.getDataObjects(), infoNode);
    if (isFlowElementLocalizationChanged || isDataObjectLocalizationChanged) {
      localizationValuesChanged = true;
    }

    if (localizationValuesChanged) {
      dynamicBpmnService.saveProcessDefinitionInfo(processDefinitionId, infoNode);
    }
  }
  
  protected boolean localizeFlowElements(Collection<FlowElement> flowElements, ObjectNode infoNode) {
    boolean localizationValuesChanged = false;
    
    if (flowElements == null) return localizationValuesChanged;
    
    CommandContext commandContext = Context.getCommandContext();
    DynamicBpmnService dynamicBpmnService = commandContext.getProcessEngineConfiguration().getDynamicBpmnService();
    
    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof UserTask || flowElement instanceof SubProcess) {
        List<ExtensionElement> localizationElements = flowElement.getExtensionElements().get("localization");
        if (localizationElements != null) {
          for (ExtensionElement localizationElement : localizationElements) {
            if (BpmnXMLConstants.ACTIVITI_EXTENSIONS_PREFIX.equals(localizationElement.getNamespacePrefix())) {
              String locale = localizationElement.getAttributeValue(null, "locale");
              String name = localizationElement.getAttributeValue(null, "name");
              String documentation = null;
              List<ExtensionElement> documentationElements = localizationElement.getChildElements().get("documentation");
              if (documentationElements != null) {
                for (ExtensionElement documentationElement : documentationElements) {
                  documentation = StringUtils.trimToNull(documentationElement.getElementText());
                  break;
                }
              }

              String flowElementId = flowElement.getId();
              if (isEqualToCurrentLocalizationValue(locale, flowElementId, "name", name, infoNode) == false) {
                dynamicBpmnService.changeLocalizationName(locale, flowElementId, name, infoNode);
                localizationValuesChanged = true;
              }
              
              if (documentation != null && isEqualToCurrentLocalizationValue(locale, flowElementId, "description", documentation, infoNode) == false) {
                dynamicBpmnService.changeLocalizationDescription(locale, flowElementId, documentation, infoNode);
                localizationValuesChanged = true;
              }
              
              break;
            }
          }
        }
        
        if (flowElement instanceof SubProcess) {
          SubProcess subprocess = (SubProcess) flowElement;
          boolean isFlowElementLocalizationChanged = localizeFlowElements(subprocess.getFlowElements(), infoNode);
          boolean isDataObjectLocalizationChanged = localizeDataObjectElements(subprocess.getDataObjects(), infoNode);
          if (isFlowElementLocalizationChanged || isDataObjectLocalizationChanged) {
            localizationValuesChanged = true;
          }
        }
      }
    }
    
    return localizationValuesChanged;
  }
  
  protected boolean isEqualToCurrentLocalizationValue(String language, String id, String propertyName, String propertyValue, ObjectNode infoNode) {
    boolean isEqual = false;
    JsonNode localizationNode = infoNode.path("localization").path(language).path(id).path(propertyName);
    if (localizationNode.isMissingNode() == false && localizationNode.isNull() == false && localizationNode.asText().equals(propertyValue)) {
      isEqual = true;
    }
    return isEqual;
  }
  
  protected boolean localizeDataObjectElements(List<ValuedDataObject> dataObjects, ObjectNode infoNode) {
    boolean localizationValuesChanged = false;
    CommandContext commandContext = Context.getCommandContext();
    DynamicBpmnService dynamicBpmnService = commandContext.getProcessEngineConfiguration().getDynamicBpmnService();

    for(ValuedDataObject dataObject : dataObjects) {
      List<ExtensionElement> localizationElements = dataObject.getExtensionElements().get("localization");
      if (localizationElements != null) {
        for (ExtensionElement localizationElement : localizationElements) {
          if (BpmnXMLConstants.ACTIVITI_EXTENSIONS_PREFIX.equals(localizationElement.getNamespacePrefix())) {
            String locale = localizationElement.getAttributeValue(null, "locale");
            String name = localizationElement.getAttributeValue(null, "name");
            String documentation = null;

            List<ExtensionElement> documentationElements = localizationElement.getChildElements().get("documentation");
            if (documentationElements != null) {
              for (ExtensionElement documentationElement : documentationElements) {
                documentation = StringUtils.trimToNull(documentationElement.getElementText());
                break;
              }
            }
            
            if (name != null && isEqualToCurrentLocalizationValue(locale, dataObject.getName(), DynamicBpmnConstants.LOCALIZATION_NAME, name, infoNode) == false) {
              dynamicBpmnService.changeLocalizationName(locale, dataObject.getName(), name, infoNode);
              localizationValuesChanged = true;
            }
            
            if (documentation != null && isEqualToCurrentLocalizationValue(locale, dataObject.getName(), 
                DynamicBpmnConstants.LOCALIZATION_DESCRIPTION, documentation, infoNode) == false) {
              
              dynamicBpmnService.changeLocalizationDescription(locale, dataObject.getName(), documentation, infoNode);
              localizationValuesChanged = true;
            }
          }
        }
      }
    }
    
    return localizationValuesChanged;
  }
  
  enum ExprType {
	  USER, GROUP
  }
  
  private void addAuthorizationsFromIterator(Set<Expression> exprSet, ProcessDefinitionEntity processDefinition, ExprType exprType) {
    if (exprSet != null) {
      Iterator<Expression> iterator = exprSet.iterator();
      while (iterator.hasNext()) {
        Expression expr = (Expression) iterator.next();
        IdentityLinkEntity identityLink = new IdentityLinkEntity();
        identityLink.setProcessDef(processDefinition);
        if (exprType.equals(ExprType.USER)) {
           identityLink.setUserId(expr.toString());
        } else if (exprType.equals(ExprType.GROUP)) {
          identityLink.setGroupId(expr.toString());
        }
        identityLink.setType(IdentityLinkType.CANDIDATE);
        identityLink.insert();
      }
    }
  }

  protected void addAuthorizations(ProcessDefinitionEntity processDefinition) {
    addAuthorizationsFromIterator(processDefinition.getCandidateStarterUserIdExpressions(), processDefinition, ExprType.USER);
    addAuthorizationsFromIterator(processDefinition.getCandidateStarterGroupIdExpressions(), processDefinition, ExprType.GROUP);
  }

  /**
   * Returns the default name of the image resource for a certain process.
   * 
   * It will first look for an image resource which matches the process
   * specifically, before resorting to an image resource which matches the BPMN
   * 2.0 xml file resource.
   * 
   * Example: if the deployment contains a BPMN 2.0 xml resource called
   * 'abc.bpmn20.xml' containing only one process with key 'myProcess', then
   * this method will look for an image resources called 'abc.myProcess.png'
   * (or .jpg, or .gif, etc.) or 'abc.png' if the previous one wasn't found.
   * 
   * Example 2: if the deployment contains a BPMN 2.0 xml resource called 
   * 'abc.bpmn20.xml' containing three processes (with keys a, b and c),
   * then this method will first look for an image resource called 'abc.a.png' 
   * before looking for 'abc.png' (likewise for b and c).
   * Note that if abc.a.png, abc.b.png and abc.c.png don't exist, all
   * processes will have the same image: abc.png.
   * 
   * @return null if no matching image resource is found.
   */
  protected String getDiagramResourceForProcess(String bpmnFileResource, String processKey, Map<String, ResourceEntity> resources) {
    for (String diagramSuffix: DIAGRAM_SUFFIXES) {
      String diagramForBpmnFileResource = getBpmnFileImageResourceName(bpmnFileResource, diagramSuffix);
      String processDiagramResource = getProcessImageResourceName(bpmnFileResource, processKey, diagramSuffix);
      if (resources.containsKey(processDiagramResource)) {
        return processDiagramResource;
      } else if (resources.containsKey(diagramForBpmnFileResource)) {
        return diagramForBpmnFileResource;
      }
    }
    return null;
  }
  
  protected String getBpmnFileImageResourceName(String bpmnFileResource, String diagramSuffix) {
    String bpmnFileResourceBase = stripBpmnFileSuffix(bpmnFileResource);
    return bpmnFileResourceBase + diagramSuffix;
  }

  protected String getProcessImageResourceName(String bpmnFileResource, String processKey, String diagramSuffix) {
    String bpmnFileResourceBase = stripBpmnFileSuffix(bpmnFileResource);
    return bpmnFileResourceBase + processKey + "." + diagramSuffix;
  }

  protected String stripBpmnFileSuffix(String bpmnFileResource) {
    for (String suffix : BPMN_RESOURCE_SUFFIXES) {
      if (bpmnFileResource.endsWith(suffix)) {
        return bpmnFileResource.substring(0, bpmnFileResource.length() - suffix.length());
      }
    }
    return bpmnFileResource;
  }

  protected void createResource(String name, byte[] bytes, DeploymentEntity deploymentEntity) {
    ResourceEntity resource = new ResourceEntity();
    resource.setName(name);
    resource.setBytes(bytes);
    resource.setDeploymentId(deploymentEntity.getId());
    
    // Mark the resource as 'generated'
    resource.setGenerated(true);
    
    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(resource);
  }
  
  protected boolean isBpmnResource(String resourceName) {
    for (String suffix : BPMN_RESOURCE_SUFFIXES) {
      if (resourceName.endsWith(suffix)) {
        return true;
      }
    }
    return false;
  }
  
  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }
  
  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }
  
  public BpmnParser getBpmnParser() {
    return bpmnParser;
  }
  
  public void setBpmnParser(BpmnParser bpmnParser) {
    this.bpmnParser = bpmnParser;
  }
  
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }
  
  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }
  
}
