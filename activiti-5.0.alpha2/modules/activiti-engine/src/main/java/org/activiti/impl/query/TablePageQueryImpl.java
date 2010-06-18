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
package org.activiti.impl.query;

import org.activiti.ActivitiException;
import org.activiti.SortOrder;
import org.activiti.TablePage;
import org.activiti.TablePageQuery;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.query.AbstractSingleResultQuery;


/**
 * 
 * @author Joram Barrez
 */
public class TablePageQueryImpl extends AbstractSingleResultQuery<TablePage> implements TablePageQuery {
  
  protected CommandExecutor commandExecutor;
  
  protected String tableName;
  
  protected int start = -1;
  
  protected int maxRows = -1;
  
  protected String sortColumn;
  
  protected SortOrder sortOrder;

  public TablePageQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public TablePageQuery tableName(String tableName) {
    this.tableName = tableName;
    return this;
  }
  
  public TablePageQuery start(int start) {
    this.start = start;
    return this;
  }
  
  public TablePageQuery size(int size) {
    this.maxRows = size;
    return this;
  }
  
  public TablePageQuery orderAsc(String column) {
    if (sortColumn != null) {
      throw new ActivitiException("Invalid usage: cannot use both orderAsc and orderDesc in same query");
    }
    this.sortOrder = SortOrder.ASCENDING;
    this.sortColumn = column;
    return this;
  }
  
  public TablePageQuery orderDesc(String column) {
    if (sortColumn != null) {
      throw new ActivitiException("Invalid usage: cannot use both orderAsc and orderDesc in same query");
    }
    this.sortOrder = SortOrder.DESCENDING;
    this.sortColumn = column;
    return this;
  }
  
  protected TablePage executeSingleResultQuery(CommandContext commandContext) {
    if (tableName == null || start == -1 || maxRows == -1) {
      throw new ActivitiException("Table name, offset and maxResults are " +
      		"minimally needed to execute a TablePageQuery");
    }
    return commandContext.getPersistenceSession()
      .getTablePage(tableName, start, maxRows, sortColumn, sortOrder);
  }

}
