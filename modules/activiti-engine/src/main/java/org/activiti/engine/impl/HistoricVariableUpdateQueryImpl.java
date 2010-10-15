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
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.history.HistoricVariableUpdateQuery;
import org.activiti.engine.history.HistoricVariableUpdateQueryProperty;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.query.QueryProperty;


/**
 * @author Tom Baeyens
 */
public class HistoricVariableUpdateQueryImpl extends AbstractQuery<HistoricVariableUpdateQuery, HistoricVariableUpdate> implements HistoricVariableUpdateQuery {

  protected String processInstanceId;
  protected String variableName;
  protected HistoricVariableUpdateQueryProperty orderProperty;

  public HistoricVariableUpdateQueryImpl() {
  }

  public HistoricVariableUpdateQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricVariableUpdateQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricVariableUpdateQueryImpl variableName(String variableName) {
    this.variableName = variableName;
    return this;
  }

  public HistoricVariableUpdateQueryImpl asc() {
    return direction(Direction.ASCENDING);
  }
  
  public HistoricVariableUpdateQueryImpl desc() {
    return direction(Direction.DESCENDING);
  }
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistorySession()
      .findHistoricVariableUpdateCountByQueryCriteria(this);
  }

  public List<HistoricVariableUpdate> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistorySession()
      .findHistoricVariableUpdatesByQueryCriteria(this, page);
  }
  
  protected void checkQueryOk() {
    if (orderProperty != null) {
      throw new ActivitiException("Invalid query: call asc() or desc() after using orderByXX()");
    }
  }

  public HistoricVariableUpdateQueryImpl direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("you should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return this;
  }

  public HistoricVariableUpdateQueryImpl orderBy(QueryProperty property) {
    if(!(property instanceof HistoricVariableUpdateQueryProperty)) {
      throw new ActivitiException("Only HistoricVariableUpdateQueryProperty can be used with orderBy");
    }
    this.orderProperty = (HistoricVariableUpdateQueryProperty) property;
    return this;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getVariableName() {
    return variableName;
  }
}
