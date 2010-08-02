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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.SortOrder;
import org.activiti.engine.TablePage;
import org.activiti.engine.TablePageQuery;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * 
 * @author Joram Barrez
 */
public class TablePageQueryImpl implements TablePageQuery {
  
  protected CommandExecutor commandExecutor;

  protected String tableName;
  protected int start = -1;
  protected int maxRows = -1;
  protected String sortColumn;
  protected SortOrder sortOrder;

  public TablePageQueryImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
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
  
  public TablePage singleResult() {
    return commandExecutor.execute(new Command<TablePage>() {
      public TablePage execute(CommandContext commandContext) {
        if (tableName == null || start == -1 || maxRows == -1) {
          throw new ActivitiException("Table name, offset and maxResults are " +
              "minimally needed to execute a TablePageQuery");
        }
        return commandContext.getManagementSession()
          .getTablePage(tableName, start, maxRows, sortColumn, sortOrder);
      };
    });
  }
}
