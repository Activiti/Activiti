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
package org.activiti.engine.management;

import java.util.ArrayList;
import java.util.List;


/**
 * Structure containing meta data (column names, column types, etc.) 
 * about a certain database table.
 * 
 * @author Joram Barrez
 */
public class TableMetaData {
  
  protected String tableName;
  
  protected List<String> columnNames = new ArrayList<String>();
  
  protected List<String> columnTypes = new ArrayList<String>();

  public TableMetaData() {
    
  }
  
  public TableMetaData(String tableName) {
    this.tableName = tableName;
  }
  
  public void addColumnMetaData(String columnName, String columnType) {
    columnNames.add(columnName);
    columnTypes.add(columnType);
  }
  
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

  public List<String> getColumnTypes() {
    return columnTypes;
  }

  public void setColumnTypes(List<String> columnTypes) {
    this.columnTypes = columnTypes;
  }

}
