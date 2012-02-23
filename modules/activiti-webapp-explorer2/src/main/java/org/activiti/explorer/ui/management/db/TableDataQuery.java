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
package org.activiti.explorer.ui.management.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ManagementService;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;
import org.activiti.explorer.data.MapItem;

import com.vaadin.data.Item;



/**
 * @author Joram Barrez
 */
public class TableDataQuery extends AbstractLazyLoadingQuery {
  
  protected String tableName;
  protected ManagementService managementService;
  protected Object[] sortPropertyIds;
  protected boolean[] sortPropertyIdsAscending;
  
  public TableDataQuery(String tableName, ManagementService managementService) {
    this.tableName = tableName;
    this.managementService = managementService;
  }
  
  public List<Item> loadItems(int start, int count) {
    TablePageQuery query = managementService.createTablePageQuery().tableName(tableName);
    
    if (sortPropertyIds != null && sortPropertyIds.length > 0) {
      for (int i=0; i<sortPropertyIds.length; i++) {
        String column = (String) sortPropertyIds[i]; // all container properties for table data are Strings
        if (sortPropertyIdsAscending[i] == true) {
          query.orderAsc(column);
        } else {
          query.orderDesc(column);
        }
      }
    }
    
    List<Map<String, Object>> rows = query.listPage(start, count).getRows();
    List<Item> items = new ArrayList<Item>();
    for (Map<String, Object> row : rows) {
      items.add(new MapItem(row));
    }
    return items;
  }

  public int size() {
    return managementService.getTableCount().get(tableName).intValue();
  }

  public void setSorting(Object[] propertyId, boolean[] ascending) {
    this.sortPropertyIds = propertyId;
    this.sortPropertyIdsAscending = ascending;
  }

  public Item loadSingleResult(String id) {
    throw new UnsupportedOperationException();
  }
  
}
