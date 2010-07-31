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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.Execution;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.ProcessInstanceQuery;
import org.activiti.engine.ProcessService;
import org.activiti.engine.impl.cmd.DeleteProcessInstanceCmd;
import org.activiti.engine.impl.cmd.FindExecutionCmd;
import org.activiti.engine.impl.cmd.FindExecutionInActivityCmd;
import org.activiti.engine.impl.cmd.FindProcessInstanceCmd;
import org.activiti.engine.impl.cmd.GetExecutionVariableCmd;
import org.activiti.engine.impl.cmd.GetExecutionVariablesCmd;
import org.activiti.engine.impl.cmd.SendEventCmd;
import org.activiti.engine.impl.cmd.SetExecutionVariablesCmd;
import org.activiti.engine.impl.cmd.StartProcessInstanceCmd;

/**
 * @author Tom Baeyens
 */
public class ProcessServiceImpl extends ServiceImpl implements ProcessService {

  public Execution findExecutionById(String id) {
    return commandExecutor.execute(new FindExecutionCmd(id));
  }

  public void deleteProcessInstance(String processInstanceId) {
    commandExecutor.execute(new DeleteProcessInstanceCmd(processInstanceId));
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, null));
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, variables));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, null));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables) {
    return commandExecutor.execute(new StartProcessInstanceCmd<ProcessInstance>(null, processDefinitionId, variables));
  }

  public ProcessInstance findProcessInstanceById(String id) {
    return commandExecutor.execute(new FindProcessInstanceCmd(id));
  }

  public ProcessInstanceQuery createProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl(commandExecutor);
  }

  public Map<String, Object> getVariables(String executionId) {
    return commandExecutor.execute(new GetExecutionVariablesCmd(executionId));
  }

  public Object getVariable(String executionId, String variableName) {
    return commandExecutor.execute(new GetExecutionVariableCmd(executionId, variableName));
  }

  public void setVariable(String executionId, String variableName, Object value) {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables));
  }

  public void setVariables(String executionId, Map<String, Object> variables) {
    commandExecutor.execute(new SetExecutionVariablesCmd(executionId, variables));
  }

  public void sendEvent(String executionId) {
    commandExecutor.execute(new SendEventCmd(executionId, null));
  }

  public void sendEvent(String executionId, Object eventData) {
    commandExecutor.execute(new SendEventCmd(executionId, eventData));
  }

  public Execution findExecutionInActivity(String processInstanceId, String activityId) {
    return commandExecutor.execute(new FindExecutionInActivityCmd(processInstanceId, activityId));
  }
}
