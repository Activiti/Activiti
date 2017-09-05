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

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.ListQueryParameterObject;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.query.Query;
import org.activiti.engine.query.QueryProperty;

/**
 * Abstract superclass for all query types.
 * 

 */
public abstract class AbstractQuery<T extends Query<?, ?>, U> extends ListQueryParameterObject implements Command<Object>, Query<T, U>, Serializable {

  private static final long serialVersionUID = 1L;

  public static final String SORTORDER_ASC = "asc";
  public static final String SORTORDER_DESC = "desc";

  private static enum ResultType {
    LIST, LIST_PAGE, SINGLE_RESULT, COUNT
  }

  protected transient CommandExecutor commandExecutor;
  protected transient CommandContext commandContext;

  protected String databaseType;

  protected String orderBy;

  protected ResultType resultType;

  protected QueryProperty orderProperty;

  public static enum NullHandlingOnOrder {
    NULLS_FIRST, NULLS_LAST
  }

  protected NullHandlingOnOrder nullHandlingOnOrder;

  protected AbstractQuery() {
    parameter = this;
  }

  protected AbstractQuery(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public AbstractQuery(CommandContext commandContext) {
    this.commandContext = commandContext;
  }

  // To be used by custom queries
  public AbstractQuery(ManagementService managementService) {
    this(((ManagementServiceImpl) managementService).getCommandExecutor());
  }

  public AbstractQuery<T, U> setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T orderBy(QueryProperty property) {
    this.orderProperty = property;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T orderBy(QueryProperty property, NullHandlingOnOrder nullHandlingOnOrder) {
    orderBy(property);
    this.nullHandlingOnOrder = nullHandlingOnOrder;
    return (T) this;
  }

  public T asc() {
    return direction(Direction.ASCENDING);
  }

  public T desc() {
    return direction(Direction.DESCENDING);
  }

  @SuppressWarnings("unchecked")
  public T direction(Direction direction) {
    if (orderProperty == null) {
      throw new ActivitiIllegalArgumentException("You should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName(), nullHandlingOnOrder);
    orderProperty = null;
    nullHandlingOnOrder = null;
    return (T) this;
  }

  protected void checkQueryOk() {
    if (orderProperty != null) {
      throw new ActivitiIllegalArgumentException("Invalid query: call asc() or desc() after using orderByXX()");
    }
  }

  @SuppressWarnings("unchecked")
  public U singleResult() {
    this.resultType = ResultType.SINGLE_RESULT;
    if (commandExecutor != null) {
      return (U) commandExecutor.execute(this);
    }
    return executeSingleResult(Context.getCommandContext());
  }

  @SuppressWarnings("unchecked")
  public List<U> list() {
    this.resultType = ResultType.LIST;
    if (commandExecutor != null) {
      return (List<U>) commandExecutor.execute(this);
    }
    return executeList(Context.getCommandContext(), null);
  }

  @SuppressWarnings("unchecked")
  public List<U> listPage(int firstResult, int maxResults) {
    this.firstResult = firstResult;
    this.maxResults = maxResults;
    this.resultType = ResultType.LIST_PAGE;
    if (commandExecutor != null) {
      return (List<U>) commandExecutor.execute(this);
    }
    return executeList(Context.getCommandContext(), new Page(firstResult, maxResults));
  }

  public long count() {
    this.resultType = ResultType.COUNT;
    if (commandExecutor != null) {
      return (Long) commandExecutor.execute(this);
    }
    return executeCount(Context.getCommandContext());
  }

  public Object execute(CommandContext commandContext) {
    if (resultType == ResultType.LIST) {
      return executeList(commandContext, null);
    } else if (resultType == ResultType.SINGLE_RESULT) {
      return executeSingleResult(commandContext);
    } else if (resultType == ResultType.LIST_PAGE) {
      return executeList(commandContext, null);
    } else {
      return executeCount(commandContext);
    }
  }

  public abstract long executeCount(CommandContext commandContext);

  /**
   * Executes the actual query to retrieve the list of results.
   * 
   * @param page
   *          used if the results must be paged. If null, no paging will be applied.
   */
  public abstract List<U> executeList(CommandContext commandContext, Page page);

  public U executeSingleResult(CommandContext commandContext) {
    List<U> results = executeList(commandContext, null);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
      throw new ActivitiException("Query return " + results.size() + " results instead of max 1");
    }
    return null;
  }

  protected void addOrder(String column, String sortOrder, NullHandlingOnOrder nullHandlingOnOrder) {

    if (orderBy == null) {
      orderBy = "";
    } else {
      orderBy = orderBy + ", ";
    }

    String defaultOrderByClause = column + " " + sortOrder;

    if (nullHandlingOnOrder != null) {

      if (nullHandlingOnOrder.equals(NullHandlingOnOrder.NULLS_FIRST)) {

        if (ProcessEngineConfigurationImpl.DATABASE_TYPE_H2.equals(databaseType) || ProcessEngineConfigurationImpl.DATABASE_TYPE_HSQL.equals(databaseType)
            || ProcessEngineConfigurationImpl.DATABASE_TYPE_POSTGRES.equals(databaseType) || ProcessEngineConfigurationImpl.DATABASE_TYPE_ORACLE.equals(databaseType)) {
          orderBy = orderBy + defaultOrderByClause + " NULLS FIRST";
        } else if (ProcessEngineConfigurationImpl.DATABASE_TYPE_MYSQL.equals(databaseType)) {
          orderBy = orderBy + "isnull(" + column + ") desc," + defaultOrderByClause;
        } else if (ProcessEngineConfigurationImpl.DATABASE_TYPE_DB2.equals(databaseType) || ProcessEngineConfigurationImpl.DATABASE_TYPE_MSSQL.equals(databaseType)) {
          orderBy = orderBy + "case when " + column + " is null then 0 else 1 end," + defaultOrderByClause;
        } else {
          orderBy = orderBy + defaultOrderByClause;
        }

      } else if (nullHandlingOnOrder.equals(NullHandlingOnOrder.NULLS_LAST)) {

        if (ProcessEngineConfigurationImpl.DATABASE_TYPE_H2.equals(databaseType) || ProcessEngineConfigurationImpl.DATABASE_TYPE_HSQL.equals(databaseType)
            || ProcessEngineConfigurationImpl.DATABASE_TYPE_POSTGRES.equals(databaseType) || ProcessEngineConfigurationImpl.DATABASE_TYPE_ORACLE.equals(databaseType)) {
          orderBy = orderBy + column + " " + sortOrder + " NULLS LAST";
        } else if (ProcessEngineConfigurationImpl.DATABASE_TYPE_MYSQL.equals(databaseType)) {
          orderBy = orderBy + "isnull(" + column + ") asc," + defaultOrderByClause;
        } else if (ProcessEngineConfigurationImpl.DATABASE_TYPE_DB2.equals(databaseType) || ProcessEngineConfigurationImpl.DATABASE_TYPE_MSSQL.equals(databaseType)) {
          orderBy = orderBy + "case when " + column + " is null then 1 else 0 end," + defaultOrderByClause;
        } else {
          orderBy = orderBy + defaultOrderByClause;
        }

      }

    } else {
      orderBy = orderBy + defaultOrderByClause;
    }

  }

  public String getOrderBy() {
    if (orderBy == null) {
      return super.getOrderBy();
    } else {
      return orderBy;
    }
  }
  
  public String getOrderByColumns() {
      return getOrderBy();
  }

  public String getDatabaseType() {
    return databaseType;
  }

  public void setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
  }

}
