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
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.history.HistoricDetailQueryProperty;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.query.QueryProperty;


/**
 * @author Tom Baeyens
 */
public class HistoricDetailQueryImpl extends AbstractQuery<HistoricDetailQuery, HistoricDetail> implements HistoricDetailQuery {

  protected String processInstanceId;
  protected String type;
  protected HistoricDetailQueryProperty orderProperty;

  public HistoricDetailQueryImpl() {
  }

  public HistoricDetailQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricDetailQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricDetailQuery onlyFormProperties() {
    this.type = "FormProperty";
    return this;
  }

  public HistoricDetailQuery onlyVariableUpdates() {
    this.type = "VariableUpdate";
    return this;
  }

  public HistoricDetailQueryImpl asc() {
    return direction(Direction.ASCENDING);
  }
  
  public HistoricDetailQueryImpl desc() {
    return direction(Direction.DESCENDING);
  }
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistorySession()
      .findHistoricDetailCountByQueryCriteria(this);
  }

  public List<HistoricDetail> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistorySession()
      .findHistoricDetailsByQueryCriteria(this, page);
  }
  
  protected void checkQueryOk() {
    if (orderProperty != null) {
      throw new ActivitiException("Invalid query: call asc() or desc() after using orderByXX()");
    }
  }

  public HistoricDetailQueryImpl direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("you should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return this;
  }

  public HistoricDetailQueryImpl orderBy(QueryProperty property) {
    if(!(property instanceof HistoricDetailQueryProperty)) {
      throw new ActivitiException("Only HistoricVariableUpdateQueryProperty can be used with orderBy");
    }
    this.orderProperty = (HistoricDetailQueryProperty) property;
    return this;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
}
