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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cmd.DeleteJobsCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.event.MessageEventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.IdentityLinkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class BpmnDeployer implements Deployer {

  private static final Logger LOG = LoggerFactory.getLogger(BpmnDeployer.class);;

  public static final String[] BPMN_RESOURCE_SUFFIXES = new String[] { "bpmn20.xml", "bpmn" };
  public static final String[] DIAGRAM_SUFFIXES = new String[]{"png", "jpg", "gif", "svg"};

  protected ExpressionManager expressionManager;
  protected BpmnParser bpmnParser;
  protected IdGenerator idGenerator;

  public void deploy(DeploymentEntity deployment) {
    LOG.debug("Processing deployment {}", deployment.getName());
    
    List<ProcessDefinitionEntity> processDefinitions = new ArrayList<ProcessDefinitionEntity>();
    Map<String, ResourceEntity> resources = deployment.getResources();

    for (String resourceName : resources.keySet()) {

      LOG.info("Processing resource {}", resourceName);
      if (isBpmnResource(resourceName)) {
        ResourceEntity resource = resources.get(resourceName);
        byte[] bytes = resource.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        
        BpmnParse bpmnParse = bpmnParser
          .createParse()
          .sourceInputStream(inputStream)
          .deployment(deployment)
          .name(resourceName);
        bpmnParse.execute();
        
        for (ProcessDefinitionEntity processDefinition: bpmnParse.getProcessDefinitions()) {
          processDefinition.setResourceName(resourceName);
          
          String diagramResourceName = getDiagramResourceForProcess(resourceName, processDefinition.getKey(), resources);
                   
          // Only generate the resource when deployment is new to prevent modification of deployment resources 
          // after the process-definition is actually deployed. Also to prevent resource-generation failure every
          // time the process definition is added to the deployment-cache when diagram-generation has failed the first time.
          if(deployment.isNew()) {
            if (Context.getProcessEngineConfiguration().isCreateDiagramOnDeploy() &&
                  diagramResourceName==null && processDefinition.isGraphicalNotationDefined()) {
              try {
                  byte[] diagramBytes = IoUtil.readInputStream(ProcessDiagramGenerator.generatePngDiagram(bpmnParse.getBpmnModel()), null);
                  diagramResourceName = getProcessImageResourceName(resourceName, processDefinition.getKey(), "png");
                  createResource(diagramResourceName, diagramBytes, deployment);
              } catch (Throwable t) { // if anything goes wrong, we don't store the image (the process will still be executable).
                LOG.warn("Error while generating process diagram, image will not be stored in repository", t);
              }
            } 
          }
          
          processDefinition.setDiagramResourceName(diagramResourceName);
          processDefinitions.add(processDefinition);
        }
      }
    }
    
    // check if there are process definitions with the same process key to prevent database unique index violation
    List<String> keyList = new ArrayList<String>();
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      if (keyList.contains(processDefinition.getKey())) {
        throw new ActivitiException("The deployment contains process definitions with the same key (process id atrribute), this is not allowed");
      }
      keyList.add(processDefinition.getKey());
    }
    
    CommandContext commandContext = Context.getCommandContext();
    ProcessDefinitionEntityManager processDefinitionManager = commandContext.getProcessDefinitionEntityManager();
    DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      
      if (deployment.isNew()) {
        int processDefinitionVersion;

        ProcessDefinitionEntity latestProcessDefinition = processDefinitionManager.findLatestProcessDefinitionByKey(processDefinition.getKey());
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

        removeObsoleteTimers(processDefinition);
        addTimerDeclarations(processDefinition);
        
        removeObsoleteMessageEventSubscriptions(processDefinition, latestProcessDefinition);
        addMessageEventSubscriptions(processDefinition);

        dbSqlSession.insert(processDefinition);
        addAuthorizations(processDefinition);

        
      } else {
        String deploymentId = deployment.getId();
        processDefinition.setDeploymentId(deploymentId);
        ProcessDefinitionEntity persistedProcessDefinition = processDefinitionManager.findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinition.getKey());
        processDefinition.setId(persistedProcessDefinition.getId());
        processDefinition.setVersion(persistedProcessDefinition.getVersion());
        processDefinition.setSuspensionState(persistedProcessDefinition.getSuspensionState());
      }

      // Add to cache
      Context
        .getProcessEngineConfiguration()
        .getDeploymentManager()
        .getProcessDefinitionCache()
        .add(processDefinition.getId(), processDefinition);
      
      // Add to deployment for further usage
      deployment.addDeployedArtifact(processDefinition);
    }
  }

  @SuppressWarnings("unchecked")
  protected void addTimerDeclarations(ProcessDefinitionEntity processDefinition) {
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(BpmnParse.PROPERTYNAME_START_TIMER);
    if (timerDeclarations!=null) {
      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
        TimerEntity timer = timerDeclaration.prepareTimerEntity(null);
        timer.setProcessDefinitionId(processDefinition.getId());
        Context
          .getCommandContext()
          .getJobEntityManager()
          .schedule(timer);
      }
    }
  }

  protected void removeObsoleteTimers(ProcessDefinitionEntity processDefinition) {
    List<Job> jobsToDelete = Context
      .getCommandContext()
      .getJobEntityManager()
      .findJobsByConfiguration(TimerStartEventJobHandler.TYPE, processDefinition.getKey());
    
    for (Job job :jobsToDelete) {
        new DeleteJobsCmd(job.getId()).execute(Context.getCommandContext());
    }
  }
  
  protected void removeObsoleteMessageEventSubscriptions(ProcessDefinitionEntity processDefinition, ProcessDefinitionEntity latestProcessDefinition) {
    // remove all subscriptions for the previous version    
    if(latestProcessDefinition != null) {
      CommandContext commandContext = Context.getCommandContext();
      
      List<EventSubscriptionEntity> subscriptionsToDelete = commandContext
        .getEventSubscriptionEntityManager()
        .findEventSubscriptionsByConfiguration(MessageEventHandler.EVENT_HANDLER_TYPE, latestProcessDefinition.getId());
      
      for (EventSubscriptionEntity eventSubscriptionEntity : subscriptionsToDelete) {
        eventSubscriptionEntity.delete();        
      } 
      
    }
  }
  
  @SuppressWarnings("unchecked")
  protected void addMessageEventSubscriptions(ProcessDefinitionEntity processDefinition) {
    CommandContext commandContext = Context.getCommandContext();
    List<EventSubscriptionDeclaration> messageEventDefinitions = (List<EventSubscriptionDeclaration>) processDefinition.getProperty(BpmnParse.PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION);
    if(messageEventDefinitions != null) {     
      for (EventSubscriptionDeclaration messageEventDefinition : messageEventDefinitions) {
        if(messageEventDefinition.isStartEvent()) {
          // look for subscriptions for the same name in db:
          List<EventSubscriptionEntity> subscriptionsForSameMessageName = commandContext
            .getEventSubscriptionEntityManager()
            .findEventSubscriptionsByName(MessageEventHandler.EVENT_HANDLER_TYPE, messageEventDefinition.getEventName());
          // also look for subscriptions created in the session:
          List<MessageEventSubscriptionEntity> cachedSubscriptions = commandContext
            .getDbSqlSession()
            .findInCache(MessageEventSubscriptionEntity.class);
          for (MessageEventSubscriptionEntity cachedSubscription : cachedSubscriptions) {
            if(messageEventDefinition.getEventName().equals(cachedSubscription.getEventName())
                    && !subscriptionsForSameMessageName.contains(cachedSubscription)) {
              subscriptionsForSameMessageName.add(cachedSubscription);
            }
          }      
          // remove subscriptions deleted in the same command
          subscriptionsForSameMessageName = commandContext
                  .getDbSqlSession()
                  .pruneDeletedEntities(subscriptionsForSameMessageName);
                
          if(!subscriptionsForSameMessageName.isEmpty()) {
            throw new ActivitiException("Cannot deploy process definition '" + processDefinition.getResourceName()
                    + "': there already is a message event subscription for the message with name '" + messageEventDefinition.getEventName() + "'.");
          }
          
          MessageEventSubscriptionEntity newSubscription = new MessageEventSubscriptionEntity();
          newSubscription.setEventName(messageEventDefinition.getEventName());
          newSubscription.setActivityId(messageEventDefinition.getActivityId());
          newSubscription.setConfiguration(processDefinition.getId());
          
          newSubscription.insert();
        }
      }
    }      
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
