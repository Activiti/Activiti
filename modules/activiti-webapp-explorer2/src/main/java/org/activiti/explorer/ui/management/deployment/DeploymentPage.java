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
package org.activiti.explorer.ui.management.deployment;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.DeploymentNavigator;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.management.ManagementPage;
import org.activiti.explorer.ui.util.ThemeImageColumnGenerator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;


/**
 * @author Joram Barrez
 */
public class DeploymentPage extends ManagementPage {

  private static final long serialVersionUID = 1L;
  
  protected String deploymentId;
  protected Table deploymentTable;
  protected LazyLoadingContainer deploymentListContainer;
  
  public DeploymentPage() {
    ExplorerApp.get().setCurrentUriFragment(
      new UriFragment(DeploymentNavigator.DEPLOYMENT_URI_PART));
  }
  
  public DeploymentPage(String deploymentId) {
    this();
    this.deploymentId = deploymentId;
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    
    if (deploymentId == null) {
      selectElement(0);
    } else {
      selectElement(deploymentListContainer.getIndexForObjectId(deploymentId));
    }
  }
  
  @Override
  protected Table createList() {
    final Table deploymentTable = new Table();
    
    LazyLoadingQuery deploymentListQuery = new DeploymentListQuery();
    deploymentListContainer = new LazyLoadingContainer(deploymentListQuery, 10);
    deploymentTable.setContainerDataSource(deploymentListContainer);
            
    // Listener to change right panel when clicked on a deployment
    deploymentTable.addListener(new Property.ValueChangeListener() {
      private static final long serialVersionUID = 8811553575319455854L;
      public void valueChange(ValueChangeEvent event) {
        Item item = deploymentTable.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
        if(item != null) {
          String deploymentId = (String) item.getItemProperty("id").getValue();
          setDetailComponent(new DeploymentDetailPanel(deploymentId, DeploymentPage.this));
          
          // Update URL
          ExplorerApp.get().setCurrentUriFragment(
            new UriFragment(DeploymentNavigator.DEPLOYMENT_URI_PART, deploymentId));
        } else {
          // Nothing is selected
          setDetailComponent(null);
          ExplorerApp.get().setCurrentUriFragment(new UriFragment(DeploymentNavigator.DEPLOYMENT_URI_PART));
        }
      }
    });
    
    // Create column headers
    deploymentTable.addGeneratedColumn("icon", new ThemeImageColumnGenerator(Images.DEPLOYMENT_22));
    deploymentTable.setColumnWidth("icon", 22);
    
    deploymentTable.addContainerProperty("name", String.class, null);
    deploymentTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    
    return deploymentTable;
  }
  
}
