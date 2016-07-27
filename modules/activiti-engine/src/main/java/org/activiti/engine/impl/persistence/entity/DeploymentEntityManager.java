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

package org.activiti.engine.impl.persistence.entity;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.DeploymentQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DeploymentEntityManager extends AbstractManager {
  
  public void insertDeployment(DeploymentEntity deployment) {
    getDbSqlSession().insert(deployment);
    
    for (ResourceEntity resource : deployment.getResources().values()) {
      resource.setDeploymentId(deployment.getId());
      getResourceManager().insertResource(resource);
    }
  }
  
  public void deleteDeployment(String deploymentId, boolean cascade) {
    List<ProcessDefinition> processDefinitions = getDbSqlSession()
            .createProcessDefinitionQuery()
            .deploymentId(deploymentId)
            .list();
    
    // Remove the deployment link from any model. 
    // The model will still exists, as a model is a source for a deployment model and has a different lifecycle
    List<Model> models = getDbSqlSession()
        .createModelQueryImpl()
        .deploymentId(deploymentId)
        .list();
    
    for (Model model : models) {
      ModelEntity modelEntity = (ModelEntity) model;
      modelEntity.setDeploymentId(null);
      getModelManager().updateModel(modelEntity);
    }
    
    if (cascade) {

      // delete process instances
      for (ProcessDefinition processDefinition: processDefinitions) {
        String processDefinitionId = processDefinition.getId();
        
        getProcessInstanceManager().deleteProcessInstancesByProcessDefinition(processDefinitionId, "deleted deployment", cascade);
      }
    }

    for (ProcessDefinition processDefinition : processDefinitions) {
      String processDefinitionId = processDefinition.getId();
      // remove related authorization parameters in IdentityLink table
      getIdentityLinkManager().deleteIdentityLinksByProcDef(processDefinitionId);
      
      // event subscriptions
      List<EventSubscriptionEntity> eventSubscriptionEntities = getEventSubscriptionManager() 
          .findEventSubscriptionsByTypeAndProcessDefinitionId(null, processDefinitionId, processDefinition.getTenantId()); // null type ==> all types
      for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptionEntities) {
        eventSubscriptionEntity.delete();
      }
      
      getProcessDefinitionInfoManager().deleteProcessDefinitionInfo(processDefinitionId);
      
    }
    
    // delete process definitions from db
    getProcessDefinitionManager().deleteProcessDefinitionsByDeploymentId(deploymentId);
    
    for (ProcessDefinition processDefinition : processDefinitions) {
      
      // remove timer start events for current process definition:
    	
    	List<Job> timerStartJobs = Context.getCommandContext().getJobEntityManager()
    			.findJobsByTypeAndProcessDefinitionId(TimerStartEventJobHandler.TYPE, processDefinition.getId());
    	if (timerStartJobs != null && timerStartJobs.size() > 0) {
    		for (Job timerStartJob : timerStartJobs) {
					if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
						Context.getProcessEngineConfiguration()
						       .getEventDispatcher()
						       .dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, timerStartJob, null, null, processDefinition.getId()));
					}

					((JobEntity) timerStartJob).delete();
    		}
    	}
    	
    	// If previous process definition version has a timer/message/signal start event, it must be added
    	ProcessDefinitionEntity latestProcessDefinition = findLatestProcessDefinition(processDefinition);

      // Only if the currently deleted process definition is the latest version, we fall back to the previous start event type
    	if (processDefinition.getId().equals(latestProcessDefinition.getId())) { 
    		
    		// Try to find a previous version (it could be some versions are missing due to deletions)
    		ProcessDefinition previousProcessDefinition = findNewLatestProcessDefinitionAfterRemovalOf(processDefinition);
    		if (previousProcessDefinition != null) {
    			
    			// Need to resolve process definition to make sure it's parsed
    			ProcessDefinitionEntity resolvedProcessDefinition = Context.getProcessEngineConfiguration()
    					.getDeploymentManager().resolveProcessDefinition((ProcessDefinitionEntity) previousProcessDefinition);
    			
    			// Timer start
    			List<TimerDeclarationImpl> timerDeclarations = 
    					(List<TimerDeclarationImpl>) resolvedProcessDefinition.getProperty(BpmnParse.PROPERTYNAME_START_TIMER);
    	    if (timerDeclarations != null) {
    	      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
    	        TimerEntity timer = timerDeclaration.prepareTimerEntity(null);
    	        timer.setProcessDefinitionId(previousProcessDefinition.getId());
    	        
    	        if (previousProcessDefinition.getTenantId() != null) {
    	        	timer.setTenantId(previousProcessDefinition.getTenantId());
    	        }
    	        
    	        Context.getCommandContext().getJobEntityManager().schedule(timer);
    	      }
    	    }
    	    
    	    // Signal / Message start
    	    List<EventSubscriptionDeclaration> signalEventDefinitions = 
    	    		(List<EventSubscriptionDeclaration>) resolvedProcessDefinition.getProperty(BpmnParse.PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION);
    	     if(signalEventDefinitions != null) {     
    	       for (EventSubscriptionDeclaration eventDefinition : signalEventDefinitions) {
    	         if(eventDefinition.getEventType().equals("signal") && eventDefinition.isStartEvent()) {
    	        	 
    	        	 SignalEventSubscriptionEntity subscriptionEntity = new SignalEventSubscriptionEntity();
    	        	 subscriptionEntity.setEventName(eventDefinition.getEventName());
    	        	 subscriptionEntity.setActivityId(eventDefinition.getActivityId());
    	        	 subscriptionEntity.setProcessDefinitionId(previousProcessDefinition.getId());
   	        		 subscriptionEntity.setTenantId(previousProcessDefinition.getTenantId());
    	        	 subscriptionEntity.insert();
    	        	 
    	         } else if (eventDefinition.getEventType().equals("message") && eventDefinition.isStartEvent()) {
    	        	 MessageEventSubscriptionEntity newSubscription = new MessageEventSubscriptionEntity();
                 newSubscription.setEventName(eventDefinition.getEventName());
                 newSubscription.setActivityId(eventDefinition.getActivityId());
                 newSubscription.setConfiguration(previousProcessDefinition.getId());
                 newSubscription.setProcessDefinitionId(previousProcessDefinition.getId());
                 newSubscription.setTenantId(previousProcessDefinition.getTenantId());
                 newSubscription.insert();
    	         }
    	       }
    	     }    
    	    
    		}
    		
    	}
    	
    }
    
    getResourceManager().deleteResourcesByDeploymentId(deploymentId);
    
    getDbSqlSession().delete("deleteDeployment", deploymentId);
  }

  protected ProcessDefinitionEntity findLatestProcessDefinition(ProcessDefinition processDefinition) {
    ProcessDefinitionEntity latestProcessDefinition = null;
    if (processDefinition.getTenantId() != null && !ProcessEngineConfiguration.NO_TENANT_ID.equals(processDefinition.getTenantId())) {
    	latestProcessDefinition = Context.getCommandContext().getProcessDefinitionEntityManager()
     			.findLatestProcessDefinitionByKeyAndTenantId(processDefinition.getKey(), processDefinition.getTenantId());
    } else {
    	latestProcessDefinition = Context.getCommandContext().getProcessDefinitionEntityManager()
     			.findLatestProcessDefinitionByKey(processDefinition.getKey());
    }
    return latestProcessDefinition;
  }
  
  protected ProcessDefinition findNewLatestProcessDefinitionAfterRemovalOf(ProcessDefinition processDefinitionToBeRemoved) {
    
    // The latest process definition is not necessarily the one with 'version -1' (some versions could have been deleted)
    // Hence, the following logic
    
    ProcessDefinitionQueryImpl query = new ProcessDefinitionQueryImpl();
    query.processDefinitionKey(processDefinitionToBeRemoved.getKey());
    
    if (processDefinitionToBeRemoved.getTenantId() != null 
        && !ProcessEngineConfiguration.NO_TENANT_ID.equals(processDefinitionToBeRemoved.getTenantId())) {
      query.processDefinitionTenantId(processDefinitionToBeRemoved.getTenantId());
    } else {
      query.processDefinitionWithoutTenantId();
    }
    
    query.processDefinitionVersionLowerThan(processDefinitionToBeRemoved.getVersion());
    query.orderByProcessDefinitionVersion().desc();
    
    List<ProcessDefinition> processDefinitions = getProcessDefinitionManager().findProcessDefinitionsByQueryCriteria(query, new Page(0, 1));
    if (processDefinitions != null && processDefinitions.size() > 0) {
      return processDefinitions.get(0);
    } 
    return null;
  }


  public DeploymentEntity findLatestDeploymentByName(String deploymentName) {
    List<?> list = getDbSqlSession().selectList("selectDeploymentsByName", deploymentName, 0, 1);
    if (list!=null && !list.isEmpty()) {
      return (DeploymentEntity) list.get(0);
    }
    return null;
  }
  
  public DeploymentEntity findDeploymentById(String deploymentId) {
    return (DeploymentEntity) getDbSqlSession().selectOne("selectDeploymentById", deploymentId);
  }
  
  public long findDeploymentCountByQueryCriteria(DeploymentQueryImpl deploymentQuery) {
    return (Long) getDbSqlSession().selectOne("selectDeploymentCountByQueryCriteria", deploymentQuery);
  }

  @SuppressWarnings("unchecked")
  public List<Deployment> findDeploymentsByQueryCriteria(DeploymentQueryImpl deploymentQuery, Page page) {
    final String query = "selectDeploymentsByQueryCriteria";
    return getDbSqlSession().selectList(query, deploymentQuery, page);
  }
  
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return getDbSqlSession().getSqlSession().selectList("selectResourceNamesByDeploymentId", deploymentId);
  }

  @SuppressWarnings("unchecked")
  public List<Deployment> findDeploymentsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectDeploymentByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findDeploymentCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectDeploymentCountByNativeQuery", parameterMap);
  }

  public void close() {
  }

  public void flush() {
  }
}
