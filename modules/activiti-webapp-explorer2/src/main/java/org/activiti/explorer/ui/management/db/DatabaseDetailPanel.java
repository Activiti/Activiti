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

import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.management.TableMetaData;
import org.activiti.explorer.Constants;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class DatabaseDetailPanel extends VerticalLayout {
  
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
    name.addStyleName(Constants.STYLE_DATABASE_DETAILS);
    addComponent(name);
    addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));
  }
  
  protected void addTableData() {
    LazyLoadingQuery lazyLoadingQuery = new TableDataQuery(tableName, managementService);
    LazyLoadingContainer lazyLoadingContainer = new LazyLoadingContainer(lazyLoadingQuery, 10);
    
    if (lazyLoadingContainer.size() > 0) {
      
      Table data = new Table();
      data.setContainerDataSource(lazyLoadingContainer);
      data.setEditable(false);
      data.setSelectable(true);
      data.setColumnReorderingAllowed(true);
      addComponent(data);
      
      data.setWidth("95%");
      data.setHeight("80%");
      data.addStyleName(Constants.STYLE_DATABASE_TABLE_ROW);
      data.addStyleName(Reindeer.TABLE_STRONG);
      data.addStyleName(Constants.STYLE_DATABASE_DETAILS);
      setExpandRatio(data, 1.0f);
      
      // Create column headers
      TableMetaData metaData = managementService.getTableMetaData(tableName);
      for (String columnName : metaData.getColumnNames()) {
        data.addContainerProperty(columnName, String.class, null);
      }
      
    } else {
      Label noDataLabel = new Label("Table contains no rows");
      noDataLabel.addStyleName(Constants.STYLE_DATABASE_DETAILS);
      addComponent(noDataLabel);
      setExpandRatio(noDataLabel, 1.0f);
    }
  }

}
