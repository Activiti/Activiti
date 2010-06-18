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
package org.activiti.impl;

import java.util.Map;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.Configuration;
import org.activiti.IdentityService;
import org.activiti.ManagementService;
import org.activiti.ProcessEngine;
import org.activiti.ProcessService;
import org.activiti.TaskService;
import org.activiti.impl.cmd.UserCommandCmd;
import org.activiti.impl.cmduser.Command;
import org.activiti.impl.db.Db;
import org.activiti.impl.jobexecutor.JobExecutor;
import org.activiti.impl.tx.TransactionContext;

/**
 * @author Tom Baeyens
 */
public class ProcessEngineImpl implements ProcessEngine {
  
  private static Logger log = Logger.getLogger(ProcessEngineImpl.class.getName());
  
  protected String name;

  protected Map<String, Object> configurations;
  protected CmdExecutor commandExecutor;
  protected JobExecutor jobExecutor;
  
  protected ProcessServiceImpl processService;
  protected IdentityServiceImpl identityService;
  protected TaskServiceImpl taskService;
  protected ManagementServiceImpl managementService;

  public ProcessEngineImpl(Configuration configuration) {
    this.name = configuration.getName();
    this.configurations = configuration.getConfigurations();
    this.commandExecutor = getConfigurationObject(Configuration.NAME_COMMANDEXECUTOR, CmdExecutor.class);

    this.processService = new ProcessServiceImpl(this);
    this.identityService = new IdentityServiceImpl(this);
    this.taskService = new TaskServiceImpl(this);
    this.managementService = new ManagementServiceImpl(this);
    
    // If we're using a SQL Database, ensure that
    //  the Schema exists and is up-to-date
    // Note - how this is done is likely to change
    //  dramatically in the future with multiple
    //  database types, cloud databases etc
    Boolean isSQL = getConfigurationObject(Configuration.NAME_PERSISTENCETYPEISSQL, Boolean.class);
    if(isSQL) {
      Db.dbSchemaCreate(this);
      Db.dbSchemaCheckVersion(this);
    }
    
    if (name==null) {
      log.info("default activiti ProcessEngine created");
    } else {
      log.info("ProcessEngine "+name+" created");
    }

    // Create and auto-start the Background Job Executor, 
    //  if one was requested
    this.jobExecutor = (JobExecutor) getConfigurationObjects().get(Configuration.NAME_JOBEXECUTOR);
    if (jobExecutor!=null) {
      jobExecutor.setProcessEngine(this);
      jobExecutor.start();
    }
  }

  public void close() {
    if (jobExecutor!=null) {
      jobExecutor.shutdownGraceful(true);
    }
    
    Db.dbSchemaDrop(this);
  }

  public Map<String, Object> getConfigurationObjects() {
    return configurations;
  }

  public <T> T getConfigurationObject(String name, Class<?> T) {
    Object configuration = configurations.get(name);
    if (configuration==null) {
      throw new ActivitiException("configuration error: object '"+name+"' is not configured");
    }
    if (!T.isAssignableFrom(configuration.getClass())) {
      throw new ActivitiException("configuration error: object '"+name+"' ("+configuration.getClass().getName()+") is not of type "+T.getName());
    }
    return (T) configurations.get(name);
  }
  
  public <T> T execute(Command<T> command) {
    return commandExecutor.execute(new UserCommandCmd<T>(command), null);
  }

  public TransactionContext openTransactionContext() {
    return new TransactionContext(this);
  }
  
  public TransactionContext createTransactionContext() {
    return new TransactionContext(this);
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

  public CmdExecutor getCmdExecutor() {
    return commandExecutor;
  }
  
  public ProcessService getProcessService() {
    return processService;
  }
  
  public String getName() {
    return name;
  }
  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }
}
