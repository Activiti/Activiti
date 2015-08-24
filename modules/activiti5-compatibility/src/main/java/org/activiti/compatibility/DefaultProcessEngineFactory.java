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

package org.activiti.compatibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.cfg.MailServerInfo;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.rules.RulesDeployer;
import org.activiti5.engine.ActivitiException;
import org.activiti5.engine.ProcessEngine;
import org.activiti5.engine.delegate.event.ActivitiEventListener;
import org.activiti5.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti5.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti5.engine.impl.bpmn.parser.factory.ListenerFactory;
import org.activiti5.engine.impl.history.HistoryLevel;
import org.activiti5.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti5.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti5.engine.parse.BpmnParseHandler;


public class DefaultProcessEngineFactory {

  /**
   * Takes in an Activiti 6 process engine config, gives back an Activiti 5 Process engine.
   */
  @SuppressWarnings("unchecked")
  public ProcessEngine buildProcessEngine(ProcessEngineConfigurationImpl activiti6Configuration) {

    // TODO: jta/spring/custom type

    org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration = null;
    if (activiti6Configuration instanceof StandaloneProcessEngineConfiguration) {
      activiti5Configuration = new org.activiti5.engine.impl.cfg.StandaloneProcessEngineConfiguration();
      
      if (activiti6Configuration.getIdGeneratorDataSource() != null) {
        activiti5Configuration.setIdGeneratorDataSource(activiti6Configuration.getIdGeneratorDataSource());
      } else if (activiti6Configuration.getIdGeneratorDataSourceJndiName() != null) {
        activiti5Configuration.setIdGeneratorDataSourceJndiName(activiti6Configuration.getIdGeneratorDataSourceJndiName());
      } else {
        activiti5Configuration.setDataSource(activiti6Configuration.getDataSource());
      }
      
      if (activiti6Configuration.getJdbcDriver() != null) {
        activiti5Configuration.setJdbcDriver(activiti6Configuration.getJdbcDriver());
      }
      if (activiti6Configuration.getJdbcUrl() != null) {
        activiti5Configuration.setJdbcUrl(activiti6Configuration.getJdbcUrl());
      }
      if (activiti6Configuration.getJdbcUsername() != null) {
        activiti5Configuration.setJdbcUsername(activiti6Configuration.getJdbcUsername());
      }
      if (activiti6Configuration.getJdbcPassword() != null) {
        activiti5Configuration.setJdbcPassword(activiti6Configuration.getJdbcPassword());
      }
      
      if (activiti6Configuration.getIdBlockSize() > 0) {
        activiti5Configuration.setIdBlockSize(activiti6Configuration.getIdBlockSize());
      }
      
      if (activiti6Configuration.getJdbcMaxActiveConnections() > 0) {
        activiti5Configuration.setJdbcMaxActiveConnections(activiti6Configuration.getJdbcMaxActiveConnections());
      }
      
      activiti5Configuration.setHistoryLevel(HistoryLevel.getHistoryLevelForKey(activiti6Configuration.getHistoryLevel().getKey()));
      
      activiti5Configuration.setMailServerDefaultFrom(activiti6Configuration.getMailServerDefaultFrom());
      activiti5Configuration.setMailServerHost(activiti6Configuration.getMailServerHost());
      activiti5Configuration.setMailServerPassword(activiti6Configuration.getMailServerPassword());
      activiti5Configuration.setMailServerPort(activiti6Configuration.getMailServerPort());
      activiti5Configuration.setMailServerUsername(activiti6Configuration.getMailServerUsername());
      activiti5Configuration.setMailServerUseSSL(activiti6Configuration.getMailServerUseSSL());
      activiti5Configuration.setMailServerUseTLS(activiti6Configuration.getMailServerUseTLS());
      if (activiti6Configuration.getMailServers() != null && activiti6Configuration.getMailServers().size() > 0) {
        for (String key : activiti6Configuration.getMailServers().keySet()) {
          MailServerInfo mailServerInfo = activiti6Configuration.getMailServers().get(key);
          org.activiti5.engine.cfg.MailServerInfo activiti5MailServerInfo = new org.activiti5.engine.cfg.MailServerInfo();
          activiti5MailServerInfo.setMailServerDefaultFrom(mailServerInfo.getMailServerDefaultFrom());
          activiti5MailServerInfo.setMailServerHost(mailServerInfo.getMailServerHost());
          activiti5MailServerInfo.setMailServerPassword(mailServerInfo.getMailServerPassword());
          activiti5MailServerInfo.setMailServerPort(mailServerInfo.getMailServerPort());
          activiti5MailServerInfo.setMailServerUsername(mailServerInfo.getMailServerUsername());
          activiti5MailServerInfo.setMailServerUseSSL(mailServerInfo.isMailServerUseSSL());
          activiti5MailServerInfo.setMailServerUseTLS(mailServerInfo.isMailServerUseTLS());
          activiti5Configuration.getMailServers().put(key, activiti5MailServerInfo);
        }
      }
      
      activiti5Configuration.setCreateDiagramOnDeploy(activiti6Configuration.isCreateDiagramOnDeploy());
      activiti5Configuration.setProcessDefinitionCacheLimit(activiti6Configuration.getProcessDefinitionCacheLimit());
      
      if (activiti6Configuration.isAsyncExecutorEnabled()) {
        activiti5Configuration.setAsyncExecutorEnabled(true);
        if (activiti6Configuration.isAsyncExecutorActivate()) {
          activiti5Configuration.setAsyncExecutorActivate(true);
        }
      }
      if (activiti6Configuration.getActiviti5AsyncExecutor() != null) {
        AsyncExecutor activiti5AsyncExecutor = (AsyncExecutor) activiti6Configuration.getActiviti5AsyncExecutor();
        activiti5Configuration.setAsyncExecutor(activiti5AsyncExecutor);
      }
      
      activiti5Configuration.setJpaCloseEntityManager(activiti6Configuration.isJpaCloseEntityManager());
      activiti5Configuration.setJpaHandleTransaction(activiti6Configuration.isJpaHandleTransaction());
      activiti5Configuration.setJpaPersistenceUnitName(activiti6Configuration.getJpaPersistenceUnitName());
      
      if (activiti6Configuration.getBeans() != null) {
        activiti5Configuration.setBeans(activiti6Configuration.getBeans());
      }
      
      if (activiti6Configuration.getActiviti5ProcessDefinitionCache() != null) {
        activiti5Configuration.setProcessDefinitionCache((DeploymentCache<ProcessDefinitionEntity>) activiti6Configuration.getActiviti5ProcessDefinitionCache());
      }
      activiti5Configuration.setProcessDefinitionCacheLimit(activiti6Configuration.getProcessDefinitionCacheLimit());
      
      if (activiti6Configuration.getActiviti5KnowledgeBaseCache() != null) {
        activiti5Configuration.setKnowledgeBaseCache((DeploymentCache<Object>) activiti6Configuration.getActiviti5KnowledgeBaseCache());
      }
      activiti5Configuration.setKnowledgeBaseCacheLimit(activiti6Configuration.getKnowledgeBaseCacheLimit());
      
      if (activiti6Configuration.getActiviti5ActivityBehaviorFactory() != null) {
        activiti5Configuration.setActivityBehaviorFactory((ActivityBehaviorFactory) activiti6Configuration.getActiviti5ActivityBehaviorFactory());
      }
      if (activiti6Configuration.getActiviti5ListenerFactory() != null) {
        activiti5Configuration.setListenerFactory((ListenerFactory) activiti6Configuration.getActiviti5ListenerFactory());
      }
      
      convertParseHandlers(activiti6Configuration, activiti5Configuration);
      
      if (activiti6Configuration.getActiviti5CustomMybatisMappers() != null) {
        activiti5Configuration.setCustomMybatisMappers(activiti6Configuration.getActiviti5CustomMybatisMappers());
      }
      
      if (activiti6Configuration.getActiviti5CustomMybatisXMLMappers() != null) {
        activiti5Configuration.setCustomMybatisXMLMappers(activiti6Configuration.getActiviti5CustomMybatisXMLMappers());
      }
      
      convertEventListeners(activiti6Configuration, activiti5Configuration);
      
      // check if we need to enable rules deployment for Activiti 5
      if (activiti6Configuration.getCustomPostDeployers() != null) {
        List<org.activiti5.engine.impl.persistence.deploy.Deployer> activiti5Deployers = new ArrayList<org.activiti5.engine.impl.persistence.deploy.Deployer>();
        for (Deployer deployer : activiti6Configuration.getCustomPostDeployers()) {
          if (deployer instanceof RulesDeployer) {
            activiti5Deployers.add(new org.activiti5.engine.impl.rules.RulesDeployer());
            break;
          }
        }
        
        if (activiti5Deployers.size() > 0) {
          if (activiti5Configuration.getCustomPostDeployers() != null) {
            activiti5Configuration.getCustomPostDeployers().addAll(activiti5Deployers);
          } else {
            activiti5Configuration.setCustomPostDeployers(activiti5Deployers);
          }
        }
      }

    } else {
      throw new ActivitiException("Unsupported process engine configuration");
    }

    return activiti5Configuration.buildProcessEngine();

  }

  protected void convertParseHandlers(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    activiti5Configuration.setPreBpmnParseHandlers(convert(activiti6Configuration.getActiviti5PreBpmnParseHandlers()));
    activiti5Configuration.setPostBpmnParseHandlers(convert(activiti6Configuration.getActiviti5PostBpmnParseHandlers()));
    activiti5Configuration.setCustomDefaultBpmnParseHandlers(convert(activiti6Configuration.getActiviti5CustomDefaultBpmnParseHandlers()));
  }
  
  protected void convertEventListeners(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    if (activiti6Configuration.getActiviti5EventListeners() != null) {
      List<ActivitiEventListener> eventListeners = new ArrayList<ActivitiEventListener>();
      for (Object eventObject : activiti6Configuration.getActiviti5EventListeners()) {
        ActivitiEventListener eventListener = (ActivitiEventListener) eventObject;
        eventListeners.add(eventListener);
      }
      activiti5Configuration.setEventListeners(eventListeners);
    }
    
    if (activiti6Configuration.getActiviti5TypedEventListeners() != null) {
      Map<String, List<ActivitiEventListener>> eventListenerMap = new HashMap<String, List<ActivitiEventListener>>();
      for (String eventKey : activiti6Configuration.getActiviti5TypedEventListeners().keySet()) {
        List<ActivitiEventListener> eventListeners = new ArrayList<ActivitiEventListener>();
        for (Object eventObject : activiti6Configuration.getActiviti5TypedEventListeners().get(eventKey)) {
          ActivitiEventListener eventListener = (ActivitiEventListener) eventObject;
          eventListeners.add(eventListener);
        }
        eventListenerMap.put(eventKey, eventListeners);
      }
      activiti5Configuration.setTypedEventListeners(eventListenerMap);
    }
  }
  
  protected List<BpmnParseHandler> convert(List<Object> activiti5BpmnParseHandlers) {
    if (activiti5BpmnParseHandlers == null) {
      return null;
    }
      
    List<BpmnParseHandler> parseHandlers = new ArrayList<BpmnParseHandler>(activiti5BpmnParseHandlers.size());
    for (Object activiti6BpmnParseHandler : activiti5BpmnParseHandlers) {
      parseHandlers.add((BpmnParseHandler) activiti6BpmnParseHandler);
    }
    return parseHandlers;
  }
  
}
