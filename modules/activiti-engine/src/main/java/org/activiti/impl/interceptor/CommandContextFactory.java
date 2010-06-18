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
package org.activiti.impl.interceptor;

import java.util.Map;

import org.activiti.impl.cfg.PersistenceSessionFactory;
import org.activiti.impl.db.IdGenerator;
import org.activiti.impl.el.ExpressionManager;
import org.activiti.impl.job.JobHandler;
import org.activiti.impl.repository.DeployerManager;
import org.activiti.impl.repository.ProcessCache;
import org.activiti.impl.scripting.ScriptingEngines;
import org.activiti.impl.variable.VariableTypes;


/**
 * @author Tom Baeyens
 */
public class CommandContextFactory {
  
  PersistenceSessionFactory persistenceSessionFactory;
  IdGenerator idGenerator;
  ProcessCache processCache;
  DeployerManager deployerManager;
  ScriptingEngines scriptingEngines;
  VariableTypes variableTypes;
  ExpressionManager expressionManager;
  Map<String, JobHandler> jobHandlers;

  public CommandContext createCommandContext(Command<?> cmd) {
    return new CommandContext(cmd, 
                              persistenceSessionFactory.openPersistenceSession(), 
                              this);
  }

  public void setPersistenceSessionFactory(PersistenceSessionFactory persistenceSessionFactory) {
    this.persistenceSessionFactory = persistenceSessionFactory;
  }
  
  public void setDbidGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }
  
  public void setProcessCache(ProcessCache processCache) {
    this.processCache = processCache;
  }
  
  public void setDeployerManager(DeployerManager deployerManager) {
    this.deployerManager = deployerManager;
  }
  
  public void setScriptingEngines(ScriptingEngines scriptingEngines) {
    this.scriptingEngines = scriptingEngines;
  }
  
  public void setTypes(VariableTypes variableTypes) {
    this.variableTypes = variableTypes;
  }
  
  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

  
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  
  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  
  public VariableTypes getVariableTypes() {
    return variableTypes;
  }

  
  public void setVariableTypes(VariableTypes variableTypes) {
    this.variableTypes = variableTypes;
  }

  
  public PersistenceSessionFactory getPersistenceSessionFactory() {
    return persistenceSessionFactory;
  }

  
  public ProcessCache getProcessCache() {
    return processCache;
  }

  
  public DeployerManager getDeployerManager() {
    return deployerManager;
  }

  
  public ScriptingEngines getScriptingEngines() {
    return scriptingEngines;
  }

  
  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public Map<String, JobHandler> getJobCommands() {
    return jobHandlers;
  }
}
