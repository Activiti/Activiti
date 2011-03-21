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
package org.activiti.explorer.ui.management;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ManagementService;
import org.activiti.explorer.data.AbstractLazyLoadingQuery;



/**
 * @author Joram Barrez
 */
public class TableDataQuery extends AbstractLazyLoadingQuery<Map<String, Object>> {
  
  protected String tableName;
  protected ManagementService managementService;
  
  public TableDataQuery(String tableName, ManagementService managementService) {
    this.tableName = tableName;
    this.managementService = managementService;
  }

  protected List<Map<String, Object>> loadBeans(int startIndex, int count) {
    System.out.println("Loading " + startIndex);
    return managementService.createTablePageQuery().tableName(tableName).listPage(startIndex, count).getRows();
  }

  public int size() {
    return managementService.getTableCount().get(tableName).intValue();
  }
  
}
