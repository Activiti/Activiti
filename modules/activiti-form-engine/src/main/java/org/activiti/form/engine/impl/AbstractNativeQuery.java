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
package org.activiti.form.engine.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.activiti.form.api.NativeQuery;
import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.impl.context.Context;
import org.activiti.form.engine.impl.interceptor.Command;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.interceptor.CommandExecutor;
import org.apache.commons.lang3.StringUtils;

/**
 * Abstract superclass for all native query types.
 * 
 * @author Tijs Rademakers
 */
public abstract class AbstractNativeQuery<T extends NativeQuery<?, ?>, U> implements Command<Object>, NativeQuery<T, U>, Serializable {

  private static final long serialVersionUID = 1L;

  private static enum ResultType {
    LIST, LIST_PAGE, SINGLE_RESULT, COUNT
  }

  protected transient CommandExecutor commandExecutor;
  protected transient CommandContext commandContext;

  protected int maxResults = Integer.MAX_VALUE;
  protected int firstResult;
  protected ResultType resultType;

  private Map<String, Object> parameters = new HashMap<String, Object>();
  private String sqlStatement;

  protected AbstractNativeQuery(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public AbstractNativeQuery(CommandContext commandContext) {
    this.commandContext = commandContext;
  }

  public AbstractNativeQuery<T, U> setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    return this;
  }

  @SuppressWarnings("unchecked")
  public T sql(String sqlStatement) {
    this.sqlStatement = sqlStatement;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T parameter(String name, Object value) {
    parameters.put(name, value);
    return (T) this;
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
    return executeList(Context.getCommandContext(), getParameterMap(), 0, Integer.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public List<U> listPage(int firstResult, int maxResults) {
    this.firstResult = firstResult;
    this.maxResults = maxResults;
    this.resultType = ResultType.LIST_PAGE;
    if (commandExecutor != null) {
      return (List<U>) commandExecutor.execute(this);
    }
    return executeList(Context.getCommandContext(), getParameterMap(), firstResult, maxResults);
  }

  public long count() {
    this.resultType = ResultType.COUNT;
    if (commandExecutor != null) {
      return (Long) commandExecutor.execute(this);
    }
    return executeCount(Context.getCommandContext(), getParameterMap());
  }

  public Object execute(CommandContext commandContext) {
    if (resultType == ResultType.LIST) {
      return executeList(commandContext, getParameterMap(), 0, Integer.MAX_VALUE);
    } else if (resultType == ResultType.LIST_PAGE) {
      Map<String, Object> parameterMap = getParameterMap();
      parameterMap.put("resultType", "LIST_PAGE");
      parameterMap.put("firstResult", firstResult);
      parameterMap.put("maxResults", maxResults);
      if (StringUtils.isNotBlank(Objects.toString(parameterMap.get("orderBy"), ""))) {
        parameterMap.put("orderByColumns", "RES." + parameterMap.get("orderBy"));
      } else {
        parameterMap.put("orderByColumns", "RES.ID_ asc");
      }

      int firstRow = firstResult + 1;
      parameterMap.put("firstRow", firstRow);
      int lastRow = 0;
      if (maxResults == Integer.MAX_VALUE) {
        lastRow = maxResults;
      } else {
        lastRow = firstResult + maxResults + 1;
      }
      parameterMap.put("lastRow", lastRow);
      return executeList(commandContext, parameterMap, firstResult, maxResults);
    } else if (resultType == ResultType.SINGLE_RESULT) {
      return executeSingleResult(commandContext);
    } else {
      return executeCount(commandContext, getParameterMap());
    }
  }

  public abstract long executeCount(CommandContext commandContext, Map<String, Object> parameterMap);

  /**
   * Executes the actual query to retrieve the list of results.
   * 
   * @param maxResults
   * @param firstResult
   * 
   * @param page
   *          used if the results must be paged. If null, no paging will be applied.
   */
  public abstract List<U> executeList(CommandContext commandContext, Map<String, Object> parameterMap, int firstResult, int maxResults);

  public U executeSingleResult(CommandContext commandContext) {
    List<U> results = executeList(commandContext, getParameterMap(), 0, Integer.MAX_VALUE);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
      throw new ActivitiFormException("Query return " + results.size() + " results instead of max 1");
    }
    return null;
  }

  private Map<String, Object> getParameterMap() {
    HashMap<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("sql", sqlStatement);
    parameterMap.putAll(parameters);
    return parameterMap;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

}
