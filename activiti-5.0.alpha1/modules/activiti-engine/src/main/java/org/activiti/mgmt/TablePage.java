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
package org.activiti.mgmt;

import java.util.List;
import java.util.Map;


/**
 * Data structure used for retrieving database table content.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TablePage {

  protected String tableName;
  
  protected List<String> columnNames;
  
  protected List<Class<?>> columnTypes;
  
  /**
   * Identifies the index of the first result stored in this TablePage. 
   * For example in a paginated table, this value identifies the record number of
   * the result on the first row.
   */
  protected long offset;
  
  /**
   * The actual content of the database table, stored as a list of mappings of
   * the form {colum name, value}.
   * 
   * This means that every map object in the list corresponds with one row in
   * the database table.
   */
  protected List<Map<String, Object>> rows;
  
  public String getTableName() {
    return tableName;
  }
  
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
  
  public List<String> getColumnNames() {
    return columnNames;
  }
  
  public void setColumnNames(List<String> columnNames) {
    this.columnNames = columnNames;
  }
  
  public List<Class< ? >> getColumnTypes() {
    return columnTypes;
  }
  
  public void setColumnTypes(List<Class< ? >> columnTypes) {
    this.columnTypes = columnTypes;
  }
  
  public long getOffset() {
    return offset;
  }
  
  public void setOffset(long offset) {
    this.offset = offset;
  }

  public long getNoOfResults() {
    return rows.size();
  }

  public List<Map<String, Object>> getRows() {
    return rows;
  }

  public void setRows(List<Map<String, Object>> rows) {
    this.rows = rows;
  }

}
