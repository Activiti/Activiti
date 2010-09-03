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

import java.util.List;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 */
public class HistoricProcessInstanceQueryImpl extends AbstractQuery<HistoricProcessInstance> implements HistoricProcessInstanceQuery {

  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  
  protected CommandExecutor commandExecutor;
  
  public HistoricProcessInstanceQueryImpl() {
  }
  
  public HistoricProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricProcessInstanceQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricProcessInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public HistoricProcessInstanceQueryImpl processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  public HistoricProcessInstanceQueryImpl orderAsc(String column) {
    super.addOrder(column, SORTORDER_ASC);
    return this;
  }
  
  public HistoricProcessInstanceQueryImpl orderDesc(String column) {
    super.addOrder(column, SORTORDER_DESC);
    return this;
  }
  
  public long executeCount(CommandContext commandContext) {
    return commandContext
      .getHistorySession()
      .findHistoricProcessInstanceCountByQueryCriteria(this);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> executeList(CommandContext commandContext, Page page) {
    return (List) commandContext
      .getHistorySession()
      .findHistoricProcessInstancesByQueryCriteria(this, page);
  }
  
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }
}
