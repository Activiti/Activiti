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

import org.activiti.DbSchemaStrategy;
import org.activiti.IdentityService;
import org.activiti.ManagementService;
import org.activiti.ProcessService;
import org.activiti.TaskService;
import org.activiti.impl.db.IdGenerator;
import org.activiti.impl.interceptor.CommandContextFactory;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.job.JobHandlers;
import org.activiti.impl.jobexecutor.JobExecutor;
import org.activiti.impl.persistence.PersistenceSessionFactory;
import org.activiti.impl.repository.DeployerManager;
import org.activiti.impl.variable.VariableTypes;

/**
 * @author Tom Baeyens
 */
public class ProcessEngineConfiguration {

  private String processEngineName;

  private ProcessService processService;
  private IdentityService identityService;
  private TaskService taskService;
  private ManagementService managementService;

  private DeployerManager deployerManager;
  private VariableTypes variableTypes;
  private JobExecutor jobExecutor;
  private JobHandlers jobHandlers;
  private IdGenerator idGenerator;
  private CommandExecutor commandExecutor;

  private DbSchemaStrategy dbSchemaStrategy;

  private CommandContextFactory commandContextFactory;
  private PersistenceSessionFactory persistenceSessionFactory;

  // getters and setters
  // //////////////////////////////////////////////////////

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
}
