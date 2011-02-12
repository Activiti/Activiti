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
import java.util.Date;
import java.util.Map;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.history.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.ClockUtil;
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
  protected boolean isGraphicalNotationDefined;
  protected Map<String, TaskDefinition> taskDefinitions;
  protected boolean hasStartFormKey;
  
  public ProcessDefinitionEntity() {
    super(null);
  }
  
  public ExecutionEntity createProcessInstance(String businessKey) {
	  ExecutionEntity processInstance = (ExecutionEntity) super.createProcessInstance();

	    CommandContext commandContext = Context.getCommandContext();

	    commandContext
	      .getDbSqlSession()
	      .insert(processInstance);
	  
	    processInstance.setExecutions(new ArrayList<ExecutionEntity>());
	    processInstance.setProcessDefinition(processDefinition);
	    // Do not initialize variable map (let it happen lazily)

	    if (businessKey != null) {
	    	processInstance.setBusinessKey(businessKey);
	    }
	    
	    // reset the process instance in order to have the db-generated process instance id available
	    processInstance.setProcessInstance(processInstance);
	    
	    String initiatorVariableName = (String) getProperty(BpmnParse.PROPERTYNAME_INITIATOR_VARIABLE_NAME);
	    if (initiatorVariableName!=null) {
	      String authenticatedUserId = Authentication.getAuthenticatedUserId();
	      processInstance.setVariable(initiatorVariableName, authenticatedUserId);
	    }
	    
	    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
	    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
	      HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity(processInstance);

	      commandContext
	        .getSession(DbSqlSession.class)
	        .insert(historicProcessInstance);
	    }
	    
	    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
	      IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();
	      
	      String processDefinitionId = processInstance.getProcessDefinitionId();
	      String processInstanceId = processInstance.getProcessInstanceId();
	      String executionId = processInstance.getId();

	      HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity();
	      historicActivityInstance.setId(Long.toString(idGenerator.getNextId()));
	      historicActivityInstance.setProcessDefinitionId(processDefinitionId);
	      historicActivityInstance.setProcessInstanceId(processInstanceId);
	      historicActivityInstance.setExecutionId(executionId);
	      historicActivityInstance.setActivityId(processInstance.getActivityId());
	      historicActivityInstance.setActivityName((String) processInstance.getActivity().getProperty("name"));
	      historicActivityInstance.setActivityType((String) processInstance.getActivity().getProperty("type"));
	      Date now = ClockUtil.getCurrentTime();
	      historicActivityInstance.setStartTime(now);
	      
	      commandContext
	        .getDbSqlSession()
	        .insert(historicActivityInstance);
	    }

	    return processInstance;
  }

  public ExecutionEntity createProcessInstance() {
    return createProcessInstance(null);
  }
  
  @Override
  protected InterpretableExecution newProcessInstance() {
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

  public boolean hasStartFormKey() {
    return hasStartFormKey;
  }
  
  public boolean getHasStartFormKey() {
    return hasStartFormKey;
  }
  
  public void setStartFormKey(boolean hasStartFormKey) {
    this.hasStartFormKey = hasStartFormKey;
  }

  public void setHasStartFormKey(boolean hasStartFormKey) {
    this.hasStartFormKey = hasStartFormKey;
  }
  
  public boolean isGraphicalNotationDefined() {
    return isGraphicalNotationDefined;
  }
  
  public void setGraphicalNotationDefined(boolean isGraphicalNotationDefined) {
    this.isGraphicalNotationDefined = isGraphicalNotationDefined;
  }
  
}
