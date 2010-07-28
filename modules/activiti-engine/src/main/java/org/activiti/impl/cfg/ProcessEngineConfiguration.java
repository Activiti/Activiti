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
package org.activiti.impl.cfg;

import java.util.Map;

import org.activiti.engine.DbSchemaStrategy;
import org.activiti.engine.HistoricDataService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessService;
import org.activiti.engine.TaskService;
import org.activiti.impl.db.IdGenerator;
import org.activiti.impl.interceptor.CommandContextFactory;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.interceptor.SessionFactory;
import org.activiti.impl.job.JobHandlers;
import org.activiti.impl.jobexecutor.JobExecutor;
import org.activiti.impl.persistence.PersistenceSessionFactory;
import org.activiti.impl.repository.DeployerManager;
import org.activiti.impl.variable.VariableTypes;
import org.activiti.pvm.event.ProcessEventBus;

/**
 * @author Tom Baeyens
 */
public class ProcessEngineConfiguration {

  protected String processEngineName;
  protected ProcessEventBus processEventBus;
  protected ProcessService processService;
  protected HistoricDataService historicDataService;
  protected IdentityService identityService;
  protected TaskService taskService;
  protected ManagementService managementService;
  protected DeployerManager deployerManager;
  protected VariableTypes variableTypes;
  protected JobExecutor jobExecutor;
  protected JobHandlers jobHandlers;
  protected IdGenerator idGenerator;
  protected CommandExecutor commandExecutor;
  protected DbSchemaStrategy dbSchemaStrategy;
  protected CommandContextFactory commandContextFactory;
  protected PersistenceSessionFactory persistenceSessionFactory;
  protected Map<Class<?>, SessionFactory> sessionFactories;


  // getters and setters //////////////////////////////////////////////////////

  public DeployerManager getDeployerManager() {
    return deployerManager;
  }

  public PersistenceSessionFactory getPersistenceSessionFactory() {
    return persistenceSessionFactory;
  }

  public void setPersistenceSessionFactory(PersistenceSessionFactory persistenceSessionFactory) {
    this.persistenceSessionFactory = persistenceSessionFactory;
  }

  public String getProcessEngineName() {
    return processEngineName;
  }

  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }

  public ProcessEventBus getProcessEventBus() {
    return processEventBus;
  }

  public void setProcessEventBus(ProcessEventBus processEventBus) {
    this.processEventBus = processEventBus;
  }

  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }

  public DbSchemaStrategy getDbSchemaStrategy() {
    return dbSchemaStrategy;
  }

  public void setDbSchemaStrategy(DbSchemaStrategy dbSchemaStrategy) {
    this.dbSchemaStrategy = dbSchemaStrategy;
  }

  public ProcessService getProcessService() {
    return processService;
  }

  public void setHistoricDataService(HistoricDataService historicDataService) {
    this.historicDataService = historicDataService;
  }

  public HistoricDataService getHistoricDataService() {
    return historicDataService;
  }

  public IdentityService getIdentityService() {
    return identityService;
  }

  public TaskService getTaskService() {
    return taskService;
  }

  public ManagementService getManagementService() {
    return managementService;
  }

  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }

  public VariableTypes getVariableTypes() {
    return variableTypes;
  }

  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public JobHandlers getJobHandlers() {
    return jobHandlers;
  }

  public void setProcessService(ProcessService processService) {
    this.processService = processService;
  }

  public void setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
  }
  
  public void setTaskService(TaskService taskService) {
    this.taskService = taskService;
  }
  
  public void setManagementService(ManagementService managementService) {
    this.managementService = managementService;
  }
  
  public void setDeployerManager(DeployerManager deployerManager) {
    this.deployerManager = deployerManager;
  }
 
  public void setVariableTypes(VariableTypes variableTypes) {
    this.variableTypes = variableTypes;
  }
  
  public void setJobExecutor(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }
 
  public void setJobHandlers(JobHandlers jobHandlers) {
    this.jobHandlers = jobHandlers;
  }
 
  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }
  
  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
  
  public void setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
  }

  public Map<Class< ? >, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }

  public void setSessionFactories(Map<Class< ? >, SessionFactory> sessionFactories) {
    this.sessionFactories = sessionFactories;
  }
}
