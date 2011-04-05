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
package org.activiti.explorer.ui.management.job;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.JobNavigationHandler;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.management.ManagementPage;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Table;


/**
 * @author Frederik Heremans
 */
public class JobPage extends ManagementPage {

  private static final long serialVersionUID = 1L;
  
  protected String jobId;
  protected Table jobTable;
  protected LazyLoadingContainer jobListContainer;
  
  public JobPage() {
    ExplorerApp.get().setCurrentUriFragment(
      new UriFragment(JobNavigationHandler.JOB_URL_PART));
  }
  
  public JobPage(String jobId) {
    this();
    this.jobId = jobId;
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    if (jobId == null) {
      selectListElement(0);
    } else {
      selectListElement(jobListContainer.getIndexForObjectId(jobId));
    }
  }
  
  @Override
  protected Table createList() {
    final Table jobTable = new Table();
    
    LazyLoadingQuery jobListQuery = new JobListQuery();
    jobListContainer = new LazyLoadingContainer(jobListQuery, 10);
    jobTable.setContainerDataSource(jobListContainer);
            
    // Listener to change right panel when clicked on a deployment
    jobTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 8811553575319455854L;
      public void valueChange(ValueChangeEvent event) {
        Item item = jobTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if(item != null) {
          String jobId = (String) item.getItemProperty("id").getValue();

          splitPanel.setSecondComponent(new JobDetailPanel(jobId, JobPage.this));
          // Update URL
          ExplorerApp.get().setCurrentUriFragment(
            new UriFragment(JobNavigationHandler.JOB_URL_PART, jobId));
        } else {
          // Nothing is selected
          splitPanel.removeComponent(splitPanel.getSecondComponent());
          ExplorerApp.get().setCurrentUriFragment(new UriFragment(JobNavigationHandler.JOB_URL_PART));
        }
      }
    });
    
    // Create column headers
    jobTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.PROCESS));
    jobTable.setColumnWidth("icon", 32);
    
    jobTable.addContainerProperty("name", String.class, null);
    jobTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    return jobTable;
  }
  
  public void refreshCurrentJobDetails() {
    if(table.getValue() != null) {
      Item selectedJob = table.getItem(table.getValue());
      splitPanel.setSecondComponent(new JobDetailPanel((String) selectedJob.getItemProperty("id").getValue(), this));
    }
  }
}
