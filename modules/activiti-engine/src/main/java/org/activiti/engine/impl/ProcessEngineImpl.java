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
import org.activiti.engine.HistoricDataService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.persistence.db.DbSqlSessionFactory;

/**
 * @author Tom Baeyens
 */
public class ProcessEngineImpl implements ProcessEngine {

  private static Logger log = Logger.getLogger(ProcessEngineImpl.class.getName());

  protected ProcessEngineConfiguration processEngineConfiguration;
  protected String name;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected HistoricDataService historicDataService;
  protected IdentityService identityService;
  protected TaskService taskService;
  protected ManagementService managementService;
  protected DbSchemaStrategy dbSchemaStrategy;
  protected JobExecutor jobExecutor;

  public ProcessEngineImpl(ProcessEngineConfiguration processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
    this.name = processEngineConfiguration.getProcessEngineName();
    this.repositoryService = processEngineConfiguration.getRepositoryService();
    this.runtimeService = processEngineConfiguration.getProcessService();
    this.historicDataService = processEngineConfiguration.getHistoricDataService();
    this.identityService = processEngineConfiguration.getIdentityService();
    this.taskService = processEngineConfiguration.getTaskService();
    this.managementService = processEngineConfiguration.getManagementService();
    this.dbSchemaStrategy = processEngineConfiguration.getDbSchemaStrategy();
    this.jobExecutor = processEngineConfiguration.getJobExecutor();
    
    DbSqlSessionFactory dbSqlSessionFactory = processEngineConfiguration.getDbSqlSessionFactory();
    if (DbSchemaStrategy.DROP_CREATE == dbSchemaStrategy) {
      try {
        dbSqlSessionFactory.dbSchemaDrop();
      } catch (RuntimeException e) {
        // ignore
      }
    }
    if (DbSchemaStrategy.CREATE_DROP == dbSchemaStrategy || DbSchemaStrategy.DROP_CREATE == dbSchemaStrategy || DbSchemaStrategy.CREATE == dbSchemaStrategy) {
      dbSqlSessionFactory.dbSchemaCreate();
    } else if (DbSchemaStrategy.CHECK_VERSION == dbSchemaStrategy) {
      dbSqlSessionFactory.dbSchemaCheckVersion();
    }

    if (name == null) {
      log.info("default activiti ProcessEngine created");
    } else {
      log.info("ProcessEngine " + name + " created");
    }

    if ((jobExecutor != null) && (jobExecutor.isAutoActivate())) {
      jobExecutor.start();
    }
  }

  public void close() {
    if ((jobExecutor != null) && (jobExecutor.isActive())) {
      jobExecutor.shutdown();
    }

    if (DbSchemaStrategy.CREATE_DROP == dbSchemaStrategy) {
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

  public HistoricDataService getHistoricDataService() {
    return historicDataService;
  }

  public RuntimeService getRuntimeService() {
    return runtimeService;
  }
  public DbSchemaStrategy getDbSchemaStrategy() {
    return dbSchemaStrategy;
  }
  public ProcessEngineConfiguration getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  public RepositoryService getRepositoryService() {
    return repositoryService;
  }
}
