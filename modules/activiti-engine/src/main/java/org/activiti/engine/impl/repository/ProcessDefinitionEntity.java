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
package org.activiti.engine.impl.repository;

import java.util.ArrayList;
import java.util.Map;

import org.activiti.engine.impl.bpmn.IOSpecification;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.history.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.runtime.VariableMap;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionEntity extends ProcessDefinitionImpl implements ProcessDefinition, PersistentObject {

  private static final long serialVersionUID = 1L;

  protected String key;
  protected int version;
  protected String category;
  protected String deploymentId;
  protected String resourceName;
  protected Integer historyLevel;
  protected StartFormHandler startFormHandler;
  protected String diagramResourceName;
  protected Map<String, TaskDefinition> taskDefinitions;
  
  public ProcessDefinitionEntity() {
    super(null);
  }
  
  public ExecutionEntity createProcessInstance(String businessKey) {
	  ExecutionEntity processInstance = (ExecutionEntity) super.createProcessInstance();

	    CommandContext commandContext = CommandContext.getCurrent();

	    commandContext
	      .getDbSqlSession()
	      .insert(processInstance);
	  
	    processInstance.setExecutions(new ArrayList<ExecutionImpl>());
	    processInstance.setProcessDefinition(processDefinition);
	    // Do not initialize variable map (let it happen lazily)

	    if (businessKey != null) {
	    	processInstance.setBusinessKey(businessKey);
	    }
	    
	    // reset the process instance in order to have the db-generated process instance id available
	    processInstance.setProcessInstance(processInstance);
	    
	    String initiatorVariableName = (String) getProperty("initiatorVariableName");
	    if (initiatorVariableName!=null) {
	      String authenticatedUserId = Authentication.getAuthenticatedUserId();
	      processInstance.setVariable(initiatorVariableName, authenticatedUserId);
	    }
	    
	    VariableMap variableMap = VariableMap.createNewInitialized(processInstance.getId(), processInstance.getId());
	    processInstance.setVariables(variableMap);

	    int historyLevel = commandContext.getProcessEngineConfiguration().getHistoryLevel();
	    if (historyLevel>=ProcessEngineConfiguration.HISTORYLEVEL_ACTIVITY) {
	      DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
	      HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity(processInstance);
	      dbSqlSession.insert(historicProcessInstance);
	    }

	    return processInstance;
  }

  public ExecutionEntity createProcessInstance() {
    return createProcessInstance(null);
  }
  
  @Override
  protected ExecutionImpl newProcessInstance() {
    return new ExecutionEntity();
  }

  public String toString() {
    return "ProcessDefinitionEntity["+id+"]";
  }


  // getters and setters //////////////////////////////////////////////////////
  
  public Object getPersistentState() {
    return ProcessDefinitionEntity.class;
  }
  
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  public int getVersion() {
    return version;
  }
  
  public void setVersion(int version) {
    this.version = version;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public Integer getHistoryLevel() {
    return historyLevel;
  }

  public void setHistoryLevel(Integer historyLevel) {
    this.historyLevel = historyLevel;
  }

  public StartFormHandler getStartFormHandler() {
    return startFormHandler;
  }

  public void setStartFormHandler(StartFormHandler startFormHandler) {
    this.startFormHandler = startFormHandler;
  }

  public Map<String, TaskDefinition> getTaskDefinitions() {
    return taskDefinitions;
  }

  public void setTaskDefinitions(Map<String, TaskDefinition> taskDefinitions) {
    this.taskDefinitions = taskDefinitions;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }
  
  public String getDiagramResourceName() {
    return diagramResourceName;
  }

  public void setDiagramResourceName(String diagramResourceName) {
    this.diagramResourceName = diagramResourceName;
  }
}
