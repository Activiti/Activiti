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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.query.QueryProperty;


/**
 * @author Tom Baeyens
 */
public class HistoricProcessInstanceQueryImpl extends AbstractQuery<HistoricProcessInstanceQuery, HistoricProcessInstance>
  implements HistoricProcessInstanceQuery {

  protected String processInstanceId;
  protected String processDefinitionId;
  protected String businessKey;
  protected boolean open = false;
  protected HistoricProcessInstanceQueryProperty orderProperty;
  
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
  
  public HistoricProcessInstanceQuery businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }
  
  public HistoricProcessInstanceQuery unfinished() {
    this.open = true;
    return this;
  }
  
  public HistoricProcessInstanceQuery orderById() {
    return orderBy(HistoricProcessInstanceQueryProperty.ID);
  }
  
  public HistoricProcessInstanceQuery orderByBusinessKey() {
    return orderBy(HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
  }
  
  public HistoricProcessInstanceQuery orderByDuration() {
    return orderBy(HistoricProcessInstanceQueryProperty.DURATION);
  }
  
  public HistoricProcessInstanceQuery orderByStartTime() {
    return orderBy(HistoricProcessInstanceQueryProperty.START_TIME);
  }
  
  public HistoricProcessInstanceQuery orderByEndTime() {
    return orderBy(HistoricProcessInstanceQueryProperty.END_TIME);
  }
  
  public HistoricProcessInstanceQuery orderByProcessDefinitionId() {
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
  }
  
  public HistoricProcessInstanceQuery orderByProcessInstanceId() {
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
  }
  
  
  public HistoricProcessInstanceQuery asc() {
    return direction(Direction.ASCENDING);
  }
  
  public HistoricProcessInstanceQuery desc() {
    return direction(Direction.DESCENDING);
  }
  
  public HistoricProcessInstanceQuery direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("you should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return this;
  }

  public HistoricProcessInstanceQuery orderBy(QueryProperty property) {
    if(!(property instanceof HistoricProcessInstanceQueryProperty)) {
      throw new ActivitiException("Only HistoricProcessInstanceQueryProperty can be used with orderBy");
    }
    this.orderProperty = (HistoricProcessInstanceQueryProperty) property;
    return this;
  }
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistorySession()
      .findHistoricProcessInstanceCountByQueryCriteria(this);
  }

  public List<HistoricProcessInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistorySession()
      .findHistoricProcessInstancesByQueryCriteria(this, page);
  }
  
  protected void checkQueryOk() {
    if (orderProperty != null) {
      throw new ActivitiException("Invalid query: call asc() or desc() after using orderByXX()");
    }
  }
  
  public String getBusinessKey() {
    return businessKey;
  }
  public boolean isOpen() {
    return open;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
}