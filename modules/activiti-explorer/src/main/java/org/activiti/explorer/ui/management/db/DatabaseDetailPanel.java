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
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Reindeer;


/**
 * @author Joram Barrez
 */
public class DatabaseDetailPanel extends DetailPanel {
  
  private static final long serialVersionUID = 1L;
  
  protected transient ManagementService managementService;
  protected I18nManager i18nManager;
  
  protected String tableName;
  
  
  public DatabaseDetailPanel(String tableName) {
    this.tableName = tableName;
    this.managementService = ProcessEngines.getDefaultProcessEngine().getManagementService();
    this.i18nManager = ExplorerApp.get().getI18nManager();
  
    addStyleName(Reindeer.LAYOUT_WHITE);
    setSizeFull();
    
    addTableName();
    addTableData();
  }
  
  protected void addTableName() {
    HorizontalLayout header = new HorizontalLayout();
    header.setWidth(100, UNITS_PERCENTAGE);
    header.addStyleName(ExplorerLayout.STYLE_TITLE_BLOCK);
    header.setSpacing(true);
    
    // TODO: use right image
    Embedded image = new Embedded(null, Images.DATABASE_50);
    header.addComponent(image);
    header.setComponentAlignment(image, Alignment.MIDDLE_LEFT);
    header.setMargin(false, false, true, false);
    
    Label name = new Label(tableName);
    name.addStyleName(Reindeer.LABEL_H2);
    header.addComponent(name);

    header.setExpandRatio(name, 1.0f);
    header.setComponentAlignment(name, Alignment.MIDDLE_LEFT);
    addDetailComponent(header);
    
    Label spacer = new Label();
    spacer.setWidth(100, UNITS_PERCENTAGE);
    spacer.addStyleName(ExplorerLayout.STYLE_DETAIL_BLOCK);
    addDetailComponent(spacer);
  }
  
  protected void addTableData() {
    LazyLoadingQuery lazyLoadingQuery = new TableDataQuery(tableName, managementService);
    LazyLoadingContainer lazyLoadingContainer = new LazyLoadingContainer(lazyLoadingQuery, 30);
    
    if (lazyLoadingContainer.size() > 0) {
      
      Table data = new Table();
      data.setContainerDataSource(lazyLoadingContainer);
      data.setEditable(false);
      data.setSelectable(true);
      data.setColumnReorderingAllowed(true);
      if (lazyLoadingQuery.size() < 10) {
        data.setPageLength(0);
      } else {
        data.setPageLength(10);
      }
      addDetailComponent(data);
      
      data.setWidth(100, UNITS_PERCENTAGE);
      data.setHeight(100, UNITS_PERCENTAGE);
      data.addStyleName(ExplorerLayout.STYLE_DATABASE_TABLE);
      setDetailExpandRatio(data, 1.0f);
      
      // Create column headers
      TableMetaData metaData = managementService.getTableMetaData(tableName);
      for (String columnName : metaData.getColumnNames()) {
        data.addContainerProperty(columnName, String.class, null);
      }
      
    } else {
      Label noDataLabel = new Label(i18nManager.getMessage(Messages.DATABASE_NO_ROWS));
      noDataLabel.addStyleName(Reindeer.LABEL_SMALL);
      addDetailComponent(noDataLabel);
      setDetailExpandRatio(noDataLabel, 1.0f);
    }
  }

}
