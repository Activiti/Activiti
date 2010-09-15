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
import org.activiti.engine.history.Direction;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricActivityInstanceQueryProperty;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 */
public class HistoricActivityInstanceQueryImpl extends AbstractQuery<HistoricActivityInstance> implements HistoricActivityInstanceQuery {
  
  protected String processDefinitionId;
  protected String processInstanceId;
  protected HistoricActivityInstanceQueryProperty orderProperty;
  protected String orderBy;


  @Override
  public long executeCount(CommandContext commandContext) {
    return commandContext
      .getHistorySession()
      .findHistoricActivityInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricActivityInstance> executeList(CommandContext commandContext, Page page) {
    return (List) commandContext
      .getHistorySession()
      .findHistoricActivityInstancesByQueryCriteria(this, page);
  }
  

  public HistoricActivityInstanceQuery processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public HistoricActivityInstanceQuery processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  // ordering /////////////////////////////////////////////////////////////////

  public HistoricActivityInstanceQuery asc() {
    return direction(Direction.ASCENDING);
  }

  public HistoricActivityInstanceQuery desc() {
    return direction(Direction.DESCENDING);
  }

  public HistoricActivityInstanceQuery direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("you should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(direction.getName(), orderProperty.getName());
    orderProperty = null;
    return this;
  }

  public HistoricActivityInstanceQuery orderBy(HistoricActivityInstanceQueryProperty property) {
    this.orderProperty = property;
    return this;
  }

  public HistoricActivityInstanceQuery orderByDuration() {
    orderBy(HistoricActivityInstanceQueryProperty.DURATION);
    return this;
  }

  public HistoricActivityInstanceQuery orderByEnd() {
    orderBy(HistoricActivityInstanceQueryProperty.END);
    return this;
  }

  public HistoricActivityInstanceQuery orderByExecutionId() {
    orderBy(HistoricActivityInstanceQueryProperty.EXECUTION_ID);
    return this;
  }

  public HistoricActivityInstanceQuery orderById() {
    orderBy(HistoricActivityInstanceQueryProperty.ID);
    return this;
  }

  public HistoricActivityInstanceQuery orderByProcessDefinitionId() {
    orderBy(HistoricActivityInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public HistoricActivityInstanceQuery orderByProcessInstanceId() {
    orderBy(HistoricActivityInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricActivityInstanceQuery orderByStart() {
    orderBy(HistoricActivityInstanceQueryProperty.START);
    return this;
  }
}
