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
package org.activiti.engine.impl;

import java.util.logging.Logger;

import org.activiti.engine.DbSchemaStrategy;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

/**
 * @author Tom Baeyens
 */
public class ProcessEngineImpl implements ProcessEngine {

  private static Logger log = Logger.getLogger(ProcessEngineImpl.class.getName());

  protected ProcessEngineConfiguration processEngineConfiguration;
  protected String name;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected HistoryService historicDataService;
  protected IdentityService identityService;
  protected TaskService taskService;
  protected FormService formService;
  protected ManagementService managementService;
  protected String dbSchemaStrategy;
  protected JobExecutor jobExecutor;
  protected CommandExecutor commandExecutor;

  public ProcessEngineImpl(ProcessEngineConfiguration processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
    this.name = processEngineConfiguration.getProcessEngineName();
    this.repositoryService = processEngineConfiguration.getRepositoryService();
    this.runtimeService = processEngineConfiguration.getProcessService();
    this.historicDataService = processEngineConfiguration.getHistoryService();
    this.identityService = processEngineConfiguration.getIdentityService();
    this.taskService = processEngineConfiguration.getTaskService();
    this.formService = processEngineConfiguration.getFormService();
    this.managementService = processEngineConfiguration.getManagementService();
    this.dbSchemaStrategy = processEngineConfiguration.getDbSchemaStrategy();
    this.jobExecutor = processEngineConfiguration.getJobExecutor();
    this.commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        performSchemaOperationsCreate();
        return null;
      }
    });

    if (name == null) {
      log.info("default activiti ProcessEngine created");
    } else {
      log.info("ProcessEngine " + name + " created");
    }

    if ((jobExecutor != null) && (jobExecutor.isAutoActivate())) {
      jobExecutor.start();
    }
  }

  protected void performSchemaOperationsCreate() {
    DbSqlSessionFactory dbSqlSessionFactory = processEngineConfiguration.getDbSqlSessionFactory();
    if (ProcessEngineConfiguration.DBSCHEMASTRATEGY_DROP_CREATE.equals(dbSchemaStrategy)) {
      try {
        dbSqlSessionFactory.dbSchemaDrop();
      } catch (RuntimeException e) {
        // ignore
      }
    }
    if ( DbSchemaStrategy.CREATE_DROP.equals(dbSchemaStrategy) 
         || ProcessEngineConfiguration.DBSCHEMASTRATEGY_DROP_CREATE.equals(dbSchemaStrategy)
         || ProcessEngineConfiguration.DBSCHEMASTRATEGY_CREATE.equals(dbSchemaStrategy)
       ) {
      dbSqlSessionFactory.dbSchemaCreate();
      
    } else if (DbSchemaStrategy.CHECK_VERSION.equals(dbSchemaStrategy)) {
      dbSqlSessionFactory.dbSchemaCheckVersion();
      
    } else if (ProcessEngineConfiguration.DBSCHEMASTRATEGY_CREATE_IF_NECESSARY.equals(dbSchemaStrategy)) {
      try {
        dbSqlSessionFactory.dbSchemaCheckVersion();
      } catch (Exception e) {
        if (e.getMessage().indexOf("no activiti tables in db")!=-1) {
          dbSqlSessionFactory.dbSchemaCreate();
        }
      }
    }
  }

  public void close() {
    ProcessEngines.unregister(this);
    if ((jobExecutor != null) && (jobExecutor.isActive())) {
      jobExecutor.shutdown();
    }

    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        performSchemaOperationsClose();
        return null;
      }
    });
  }

  private void performSchemaOperationsClose() {
    if (DbSchemaStrategy.CREATE_DROP.equals(dbSchemaStrategy)) {
      DbSqlSessionFactory dbSqlSessionFactory = processEngineConfiguration.getDbSqlSessionFactory();
      dbSqlSessionFactory.dbSchemaDrop();
    }
  }

  public String getName() {
    return name;
  }

  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }

  public IdentityService getIdentityService() {
    return identityService;
  }

  public ManagementService getManagementService() {
    return managementService;
  }

  public TaskService getTaskService() {
    return taskService;
  }

  public HistoryService getHistoryService() {
    return historicDataService;
  }

  public RuntimeService getRuntimeService() {
    return runtimeService;
  }
  
  public String getDbSchemaStrategy() {
    return dbSchemaStrategy;
  }
  
  public ProcessEngineConfiguration getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  public RepositoryService getRepositoryService() {
    return repositoryService;
  }
  
  public FormService getFormService() {
    return formService;
  }
}
