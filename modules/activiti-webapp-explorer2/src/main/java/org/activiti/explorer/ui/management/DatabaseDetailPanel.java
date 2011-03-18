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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.management.TableMetaData;
import org.vaadin.addons.lazyquerycontainer.BeanQueryFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class DatabaseDetailPanel extends Panel {
  
  private static final long serialVersionUID = -1323766637526417129L;
  
  protected String tableName;
  protected ManagementService managementService;
  
  public DatabaseDetailPanel(String tableName) {
    this.tableName = tableName;
    this.managementService = ProcessEngines.getDefaultProcessEngine().getManagementService();
    
    addStyleName(Reindeer.LAYOUT_WHITE);
    setSizeFull();
    
    addTableName();
    addTableData();
  }
  
  protected void addTableName() {
    Label name = new Label(tableName);
    name.addStyleName(Reindeer.LABEL_H1);
    addComponent(name);
    addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
  }
  
  protected void addTableData() {
    Table data = new Table();
    data.setSizeFull();
    data.setEditable(false);
    data.setSelectable(true);
    addComponent(data);

    // Actual data is provided by a lazy loading container
    BeanQueryFactory<TableDataQuery> queryFactory = new BeanQueryFactory<TableDataQuery>(TableDataQuery.class);
    Map<String,Object> queryConfiguration = new HashMap<String,Object>();
    queryConfiguration.put("managementService", managementService);
    queryConfiguration.put("tableName", tableName);
    queryFactory.setQueryConfiguration(queryConfiguration);

    LazyQueryContainer container = new LazyQueryContainer(queryFactory, false, 25);
    data.setContainerDataSource(container);
    
    // Create column headers
    TableMetaData metaData = managementService.getTableMetaData(tableName);
    for (String columnName : metaData.getColumnNames()) {
      data.addContainerProperty(columnName, String.class, null);
    }
  }

}
