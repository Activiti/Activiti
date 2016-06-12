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
import java.util.List;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.rules.RulesDeployer;
import org.activiti5.engine.ActivitiException;
import org.activiti5.engine.ProcessEngine;
import org.activiti5.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti5.engine.impl.bpmn.parser.factory.ListenerFactory;
import org.activiti5.engine.impl.history.HistoryLevel;
import org.activiti5.engine.parse.BpmnParseHandler;


public class DefaultProcessEngineFactory {

  /**
   * Takes in an Activiti 6 process engine config, gives back an Activiti 5 Process engine.
   */
  public ProcessEngine buildProcessEngine(ProcessEngineConfigurationImpl activiti6Configuration) {
    org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration = null;
    if (activiti6Configuration instanceof StandaloneProcessEngineConfiguration) {
      activiti5Configuration = new org.activiti5.engine.impl.cfg.StandaloneProcessEngineConfiguration();
      copyConfigItems(activiti6Configuration, activiti5Configuration);
      return activiti5Configuration.buildProcessEngine();
    } else {
      throw new ActivitiException("Unsupported process engine configuration");
    }
  }
   
  protected void copyConfigItems(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    activiti5Configuration.setActiviti5CompatibilityHandler(activiti6Configuration.getActiviti5CompatibilityHandler());
    
    copyJdbcConfig(activiti6Configuration, activiti5Configuration);
    copyHistoryConfig(activiti6Configuration, activiti5Configuration);
    copyMailConfig(activiti6Configuration, activiti5Configuration);
    copyDiagramConfig(activiti6Configuration, activiti5Configuration);
    copyAsyncExecutorConfig(activiti6Configuration, activiti5Configuration);
    copyJpaConfig(activiti6Configuration, activiti5Configuration);
    copyBeans(activiti6Configuration, activiti5Configuration);
    copyCaches(activiti6Configuration, activiti5Configuration);
    copyActivityBehaviorFactory(activiti6Configuration, activiti5Configuration);
    copyListenerFactory(activiti6Configuration, activiti5Configuration);
    convertParseHandlers(activiti6Configuration, activiti5Configuration);
    copyCustomMybatisMappers(activiti6Configuration, activiti5Configuration);
    activiti5Configuration.setEventDispatcher(activiti6Configuration.getEventDispatcher());
    copyPostDeployers(activiti6Configuration, activiti5Configuration);
    activiti5Configuration.setBusinessCalendarManager(activiti6Configuration.getBusinessCalendarManager());
  }

  protected void copyJdbcConfig(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
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
    
    if (activiti6Configuration.getDatabaseTablePrefix() != null) {
      activiti5Configuration.setDatabaseTablePrefix(activiti6Configuration.getDatabaseTablePrefix());
    }
  }

  protected void copyHistoryConfig(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    activiti5Configuration.setHistoryLevel(HistoryLevel.getHistoryLevelForKey(activiti6Configuration.getHistoryLevel().getKey()));
  }

  protected void copyDiagramConfig(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    activiti5Configuration.setCreateDiagramOnDeploy(activiti6Configuration.isCreateDiagramOnDeploy());
    activiti5Configuration.setActivityFontName(activiti6Configuration.getActivityFontName());
    activiti5Configuration.setLabelFontName(activiti6Configuration.getLabelFontName());
    activiti5Configuration.setAnnotationFontName(activiti6Configuration.getAnnotationFontName());
  }

  protected void copyMailConfig(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    activiti5Configuration.setMailServerDefaultFrom(activiti6Configuration.getMailServerDefaultFrom());
    activiti5Configuration.setMailServerHost(activiti6Configuration.getMailServerHost());
    activiti5Configuration.setMailServerPassword(activiti6Configuration.getMailServerPassword());
    activiti5Configuration.setMailServerPort(activiti6Configuration.getMailServerPort());
    activiti5Configuration.setMailServerUsername(activiti6Configuration.getMailServerUsername());
    activiti5Configuration.setMailServerUseSSL(activiti6Configuration.getMailServerUseSSL());
    activiti5Configuration.setMailServerUseTLS(activiti6Configuration.getMailServerUseTLS());
    if (activiti6Configuration.getMailServers() != null && activiti6Configuration.getMailServers().size() > 0) {
      activiti5Configuration.getMailServers().putAll(activiti6Configuration.getMailServers());
    }
    
    if (activiti6Configuration.getMailSessionJndi() != null) {
      activiti5Configuration.setMailSessionJndi(activiti6Configuration.getMailSessionJndi());
    }
  }

  protected void copyAsyncExecutorConfig(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    if (activiti6Configuration.isAsyncExecutorActivate()) {
      activiti5Configuration.setAsyncExecutorActivate(true);
    }
    activiti5Configuration.setDefaultFailedJobWaitTime(activiti6Configuration.getDefaultFailedJobWaitTime());
    activiti5Configuration.setAsyncFailedJobWaitTime(activiti6Configuration.getAsyncFailedJobWaitTime());
    activiti5Configuration.setAsyncExecutor(activiti6Configuration.getAsyncExecutor());
  }

  protected void copyJpaConfig(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    activiti5Configuration.setJpaCloseEntityManager(activiti6Configuration.isJpaCloseEntityManager());
    activiti5Configuration.setJpaHandleTransaction(activiti6Configuration.isJpaHandleTransaction());
    
    // We want to reuse the entity manager factory between the two engines
    if (activiti6Configuration.getJpaEntityManagerFactory() != null) {
      activiti5Configuration.setJpaEntityManagerFactory(activiti6Configuration.getJpaEntityManagerFactory());
    } else {
      activiti5Configuration.setJpaPersistenceUnitName(activiti6Configuration.getJpaPersistenceUnitName());
    }
  }

  protected void copyBeans(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    if (activiti6Configuration.getBeans() != null) {
      activiti5Configuration.setBeans(activiti6Configuration.getBeans());
    }
  }

  protected void copyCaches(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    activiti5Configuration.setProcessDefinitionCacheLimit(activiti6Configuration.getProcessDefinitionCacheLimit());
    activiti5Configuration.setEnableProcessDefinitionInfoCache(activiti6Configuration.isEnableProcessDefinitionInfoCache());
    activiti5Configuration.setProcessDefinitionCache(activiti6Configuration.getProcessDefinitionCache());
    
    activiti5Configuration.setKnowledgeBaseCacheLimit(activiti6Configuration.getKnowledgeBaseCacheLimit());
    activiti5Configuration.setKnowledgeBaseCache(activiti6Configuration.getKnowledgeBaseCache());
  }

  protected void copyActivityBehaviorFactory(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    if (activiti6Configuration.getActiviti5ActivityBehaviorFactory() != null) {
      activiti5Configuration.setActivityBehaviorFactory((ActivityBehaviorFactory) activiti6Configuration.getActiviti5ActivityBehaviorFactory());
    }
  }

  protected void copyListenerFactory(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    if (activiti6Configuration.getActiviti5ListenerFactory() != null) {
      activiti5Configuration.setListenerFactory((ListenerFactory) activiti6Configuration.getActiviti5ListenerFactory());
    }
  }

  protected void copyCustomMybatisMappers(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    if (activiti6Configuration.getActiviti5CustomMybatisMappers() != null) {
      activiti5Configuration.setCustomMybatisMappers(activiti6Configuration.getActiviti5CustomMybatisMappers());
    }
    
    if (activiti6Configuration.getActiviti5CustomMybatisXMLMappers() != null) {
      activiti5Configuration.setCustomMybatisXMLMappers(activiti6Configuration.getActiviti5CustomMybatisXMLMappers());
    }
  }

  protected void copyPostDeployers(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
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
  }

  protected void convertParseHandlers(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    activiti5Configuration.setPreBpmnParseHandlers(convert(activiti6Configuration.getActiviti5PreBpmnParseHandlers()));
    activiti5Configuration.setPostBpmnParseHandlers(convert(activiti6Configuration.getActiviti5PostBpmnParseHandlers()));
    activiti5Configuration.setCustomDefaultBpmnParseHandlers(convert(activiti6Configuration.getActiviti5CustomDefaultBpmnParseHandlers()));
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
