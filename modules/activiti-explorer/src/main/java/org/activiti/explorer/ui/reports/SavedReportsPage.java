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
package org.activiti.explorer.ui.reports;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.SavedReportNavigator;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.AbstractTablePage;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;


/**
 * @author Frederik Heremans
 */
public class SavedReportsPage extends AbstractTablePage {

  private static final long serialVersionUID = -5259331126409002997L;
  
  protected String reportId;
  protected Table reportTable;
  protected LazyLoadingQuery reportListQuery;
  protected LazyLoadingContainer reportListContainer;
  
  
  public SavedReportsPage(String reportId) {
    this.reportId = reportId;
  }
  
  public SavedReportsPage() {
    this(null);
  }

  protected Table createList() {
    reportTable = new Table();
    reportListQuery = new SavedReportsListQuery();
    reportListContainer = new LazyLoadingContainer(reportListQuery);
    reportTable.setContainerDataSource(reportListContainer);
    
    // Column headers
    reportTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.REPORT_22));
    reportTable.setColumnWidth("icon", 22);
    
    reportTable.addContainerProperty("name", String.class, null);
    reportTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
            
    // Listener to change right panel when clicked on a report
    reportTable.addListener(new Property.ValueChangeListener() {
      
      private static final long serialVersionUID = 1L;
      
      public void valueChange(ValueChangeEvent event) {
        Item item = reportTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if (item != null) {
          String historicProcessInstanceId = (String) item.getItemProperty("id").getValue();
          setDetailComponent(new SavedReportDetailPanel(historicProcessInstanceId));
          
          // Update URL
          ExplorerApp.get().setCurrentUriFragment(
                  new UriFragment(SavedReportNavigator.SAVED_REPORT_URI_PART, historicProcessInstanceId));
          
        } else {
          // Nothing selected
          setDetailComponent(null);
          ExplorerApp.get().setCurrentUriFragment(new UriFragment(SavedReportNavigator.SAVED_REPORT_URI_PART));
        }
      }
      
    });
    
    return reportTable;
  }

  protected ToolBar createMenuBar() {
    return new ReportsMenuBar();
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    
    if(reportId != null) {
      selectElement(reportListContainer.getIndexForObjectId(reportId));
    } else {
      selectElement(0);
    }
  }

}
