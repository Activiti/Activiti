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

package org.activiti.engine.impl.cmd;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.history.HistoricFormPropertyEntity;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 */
public class SubmitStartFormCmd implements Command<ProcessInstance> {

  protected String processDefinitionId;
  protected Map<String, String> properties;
  
  public SubmitStartFormCmd(String processDefinitionId, Map<String, String> properties) {
    this.processDefinitionId = processDefinitionId;
    this.properties = properties;
  }

  public ProcessInstance execute(CommandContext commandContext) {
    RepositorySession repositorySession = commandContext.getRepositorySession();
    ProcessDefinitionEntity processDefinition = repositorySession.findDeployedProcessDefinitionById(processDefinitionId);
    if (processDefinition == null) {
      throw new ActivitiException("No process definition found for id = '" + processDefinitionId + "'");
    }
    
    ExecutionEntity processInstance = null;
    processInstance = processDefinition.createProcessInstance();

    int historyLevel = commandContext.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);

      if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        for (String propertyId: properties.keySet()) {
          String propertyValue = properties.get(propertyId);
          HistoricFormPropertyEntity historicFormProperty = new HistoricFormPropertyEntity(processInstance, propertyId, propertyValue);
          dbSqlSession.insert(historicFormProperty);
        }
      }
    }
    
    StartFormHandler startFormHandler = processDefinition.getStartFormHandler();
    startFormHandler.submitFormProperties(properties, processInstance);

    processInstance.start();
    
    return processInstance;
  }
}
