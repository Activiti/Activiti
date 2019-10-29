/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.history.NativeHistoricActivityInstanceQuery;
import org.activiti.engine.history.NativeHistoricDetailQuery;
import org.activiti.engine.history.NativeHistoricProcessInstanceQuery;
import org.activiti.engine.history.NativeHistoricTaskInstanceQuery;
import org.activiti.engine.history.NativeHistoricVariableInstanceQuery;
import org.activiti.engine.history.ProcessInstanceHistoryLogQuery;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.DeleteHistoricProcessInstanceCmd;
import org.activiti.engine.impl.cmd.DeleteHistoricTaskInstanceCmd;
import org.activiti.engine.impl.cmd.GetHistoricIdentityLinksForTaskCmd;

/**



 */
public class HistoryServiceImpl extends ServiceImpl implements HistoryService {


  public HistoryServiceImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  public HistoricProcessInstanceQuery createHistoricProcessInstanceQuery() {
    return new HistoricProcessInstanceQueryImpl(commandExecutor);
  }

  public HistoricActivityInstanceQuery createHistoricActivityInstanceQuery() {
    return new HistoricActivityInstanceQueryImpl(commandExecutor);
  }

  public HistoricTaskInstanceQuery createHistoricTaskInstanceQuery() {
    return new HistoricTaskInstanceQueryImpl(commandExecutor, processEngineConfiguration.getDatabaseType());
  }

  public HistoricDetailQuery createHistoricDetailQuery() {
    return new HistoricDetailQueryImpl(commandExecutor);
  }

  @Override
  public NativeHistoricDetailQuery createNativeHistoricDetailQuery() {
    return new NativeHistoricDetailQueryImpl(commandExecutor);
  }

  public HistoricVariableInstanceQuery createHistoricVariableInstanceQuery() {
    return new HistoricVariableInstanceQueryImpl(commandExecutor);
  }

  @Override
  public NativeHistoricVariableInstanceQuery createNativeHistoricVariableInstanceQuery() {
    return new NativeHistoricVariableInstanceQueryImpl(commandExecutor);
  }

  public void deleteHistoricTaskInstance(String taskId) {
    commandExecutor.execute(new DeleteHistoricTaskInstanceCmd(taskId));
  }

  public void deleteHistoricProcessInstance(String processInstanceId) {
    commandExecutor.execute(new DeleteHistoricProcessInstanceCmd(processInstanceId));
  }

  public NativeHistoricProcessInstanceQuery createNativeHistoricProcessInstanceQuery() {
    return new NativeHistoricProcessInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricTaskInstanceQuery createNativeHistoricTaskInstanceQuery() {
    return new NativeHistoricTaskInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricActivityInstanceQuery createNativeHistoricActivityInstanceQuery() {
    return new NativeHistoricActivityInstanceQueryImpl(commandExecutor);
  }

  @Override
  public List<HistoricIdentityLink> getHistoricIdentityLinksForProcessInstance(String processInstanceId) {
    return commandExecutor.execute(new GetHistoricIdentityLinksForTaskCmd(null, processInstanceId));
  }

  @Override
  public List<HistoricIdentityLink> getHistoricIdentityLinksForTask(String taskId) {
    return commandExecutor.execute(new GetHistoricIdentityLinksForTaskCmd(taskId, null));
  }

  @Override
  public ProcessInstanceHistoryLogQuery createProcessInstanceHistoryLogQuery(String processInstanceId) {
    return new ProcessInstanceHistoryLogQueryImpl(commandExecutor, processInstanceId);
  }

}
