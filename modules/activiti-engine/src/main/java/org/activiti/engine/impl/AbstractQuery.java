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
import org.activiti.engine.Page;
import org.activiti.engine.SortOrder;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * Abstract superclass for all query types.
 *  
 * @author Joram Barrez
 */
public abstract class AbstractQuery<T> implements Command<Object>{
  
  private static enum ResultType {
    LIST, PAGINATED_LIST, SINGLE_RESULT, COUNT
  }
    
  protected CommandExecutor commandExecutor;
  protected String sortColumn;
  protected SortOrder sortOrder;
  
  protected int start;
  protected int size;
  protected ResultType resultType;
 
  protected AbstractQuery() {
  }

  protected AbstractQuery(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  @SuppressWarnings("unchecked")
  public T singleResult() {
    this.resultType = ResultType.SINGLE_RESULT;
    return (T) commandExecutor.execute(this);
  }

  @SuppressWarnings("unchecked")
  public List<T> list() {
    this.resultType = ResultType.LIST;
    return (List) commandExecutor.execute(this);
  }
  
  @SuppressWarnings("unchecked")
  public List<T> paginatedList(int start, int size) {
    this.start = start;
    this.size = size;
    this.resultType = ResultType.PAGINATED_LIST;
    return (List) commandExecutor.execute(this);
  }
  
  public long count() {
    this.resultType = ResultType.COUNT;
    return (Long) commandExecutor.execute(this);
  }
  
  public Object execute(CommandContext commandContext) {
    if (resultType==ResultType.LIST) {
      return executeList(commandContext, null);
    } else if (resultType==ResultType.SINGLE_RESULT) {
      return executeSingleResult(commandContext);
    } else if (resultType==ResultType.PAGINATED_LIST) {
      return executeList(commandContext, new Page(start, size));
    } else {
      return executeCount(commandContext);
    }
  }

  public abstract long executeCount(CommandContext commandContext);
  
  /**
   * Executes the actual query to retrieve the list of results.
   * @param page used if the results must be paged. If null, no paging will be applied. 
   */
  public abstract List<T> executeList(CommandContext commandContext, Page page);
  
  public T executeSingleResult(CommandContext commandContext) {
    List<T> results = list();
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
     throw new ActivitiException("Query return "+results.size()+" results instead of max 1");
    } 
    return null;
  }


  protected void orderAscToBeOverridden(String column) {
    if (sortColumn != null) {
      throw new ActivitiException("Invalid usage: cannot use both orderAsc and orderDesc in same query");
    }
    this.sortOrder = SortOrder.ASC;
    this.sortColumn = column;
  }
  
  public void orderDescToBeOverridden(String column) {
    if (sortColumn != null) {
      throw new ActivitiException("Invalid usage: cannot use both orderAsc and orderDesc in same query");
    }
    this.sortOrder = SortOrder.DESC;
    this.sortColumn = column;
  }
  
  public String getSortColumn() {
    return sortColumn;
  }

  public SortOrder getSortOrder() {
    return sortOrder;
  }
}
