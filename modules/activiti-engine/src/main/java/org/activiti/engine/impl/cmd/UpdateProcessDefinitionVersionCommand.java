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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * Command that update process definition id with new version.
 *
 * @author: Henry Yan
 */
public class UpdateProcessDefinitionVersionCommand implements Command<Void> {

  protected String processInstanceId;
  protected Integer version;

  public UpdateProcessDefinitionVersionCommand(String processInstanceId, Integer version) {
    this.processInstanceId = processInstanceId;
    this.version = version;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    RepositoryService repositoryService = processEngineConfiguration.getRepositoryService();
    RuntimeService runtimeService = processEngineConfiguration.getRuntimeService();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();
    if (processInstance == null) {
      throw new ActivitiException("Can't find process instance by id with " + processInstanceId);
    }

    String processDefinitionId = processInstance.getProcessDefinitionId();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionId(processDefinitionId).singleResult();
    if (processDefinition == null) {
      throw new ActivitiException("Can't find process definition by id with " + processDefinitionId);
    }

    ProcessDefinitionQuery newProcessDefinitionQuery = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(processDefinition.getKey())
        .processDefinitionVersion(version);
    long count = newProcessDefinitionQuery.count();
    if (count == 0) {
      throw new ActivitiException("Can't update process definition version: can't find version of " + version);
    }

    ProcessDefinition newProcessDefinition = newProcessDefinitionQuery.singleResult();
    String newProcessDefinitionId = newProcessDefinition.getId();

    commandContext.getExecutionEntityManager().updateProcessDefinitionVersion(processInstanceId, newProcessDefinitionId);
    commandContext.getTaskEntityManager().updateProcessDefinitionVersion(processInstanceId, newProcessDefinitionId);
    commandContext.getHistoryManager().updateProcessDefinitionVersion(processInstanceId, newProcessDefinitionId);

    return null;
  }
}
