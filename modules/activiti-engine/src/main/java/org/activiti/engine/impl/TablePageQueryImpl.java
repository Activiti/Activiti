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

import org.activiti.engine.TablePage;
import org.activiti.engine.TablePageQuery;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * 
 * @author Joram Barrez
 */
public class TablePageQueryImpl implements TablePageQuery, Command<TablePage> {
  
  CommandExecutor commandExecutor;
  
  protected String tableName;
  protected String orderBy;
  protected int firstResult;
  protected int maxResults;
  protected String sortColumn;
  protected String sortOrder;

  public TablePageQueryImpl() {
  }
  
  public TablePageQueryImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
  
  public TablePageQueryImpl tableName(String tableName) {
    this.tableName = tableName;
    return this;
  }
  
  public TablePageQueryImpl orderAsc(String column) {
    addOrder(column, AbstractQuery.SORTORDER_ASC);
    return this;
  }
  
  public TablePageQueryImpl orderDesc(String column) {
    addOrder(column, AbstractQuery.SORTORDER_DESC);
    return this;
  }
  
  public String getTableName() {
    return tableName;
  }

  protected void addOrder(String column, String sortOrder) {
    this.sortColumn = column;
    this.sortOrder = sortOrder;
    if (orderBy==null) {
      orderBy = "";
    } else {
      orderBy = orderBy+", ";
    }
    orderBy = orderBy+column+" "+sortOrder;
  }

  public TablePage listPage(int firstResult, int maxResults) {
    this.firstResult = firstResult;
    this.maxResults = maxResults;
    return commandExecutor.execute(this);
  }

  public TablePage execute(CommandContext commandContext) {
    return commandContext
      .getManagementSession()
      .getTablePage(this, firstResult, maxResults);
  }

  
  public String getSortColumn() {
    return sortColumn;
  }

  
  public String getSortOrder() {
    return sortOrder;
  }
  
  
}
