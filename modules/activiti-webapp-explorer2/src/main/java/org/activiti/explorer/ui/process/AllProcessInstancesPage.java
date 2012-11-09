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

package org.activiti.explorer.ui.process;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.AllProcessesNavigator;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.AbstractTreePage;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.management.process.ProcessInstanceDetailPanel;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Tree;

import static org.activiti.explorer.ui.process.ProcessInstanceItem.*;
	
/**
 * @author Michel Daviot
 */
public class AllProcessInstancesPage extends AbstractTreePage {

	  private static final long serialVersionUID = 1L;

	  protected Container.Hierarchical processInstanceListContainer;
	  protected LazyLoadingQuery lazyLoadingQuery;
	  
	  @Override
	  protected ToolBar createMenuBar() {
	    return new ProcessMenuBar();
	  }
	  
	  @Override
	protected Tree createTree() {
		final Tree tree=new Tree();
		tree.setImmediate(true);
		tree.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 2994322854323740189L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Item item = tree.getItem(event.getProperty().getValue()); // the value of the property is the itemId of the table entry
				if (item != null) {
			          String processInstanceId = (String) item.getItemProperty("id").getValue();
			          setDetailComponent(new ProcessInstanceDetailPanel(processInstanceId, AllProcessInstancesPage.this));
			          
			          UriFragment taskFragment = getUriFragment(processInstanceId);
			          ExplorerApp.get().setCurrentUriFragment(taskFragment);
				} else {
			          // Nothing is selected
			          setDetailComponent(null);
			          UriFragment taskFragment = getUriFragment(null);
			          ExplorerApp.get().setCurrentUriFragment(taskFragment);
				}
			}
		});
		this.lazyLoadingQuery = createLazyLoadingQuery();
		this.processInstanceListContainer = new ProcessInstanceContainer(lazyLoadingQuery);
		tree.addContainerProperty("name", String.class, null);
		tree.setContainerDataSource(processInstanceListContainer);
		// Expand whole tree
		for (Object id : tree.rootItemIds()) {
			tree.expandItemsRecursively(id);
		}

		// Set tree to show the 'name' property as caption for items
		tree.setItemCaptionPropertyId(PROPERTY_NAME);
		tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
		return tree;
	}


	

  protected String processInstanceId;
  
  protected RepositoryService repositoryService;
  protected HistoryService historyService;
  
  public AllProcessInstancesPage() {
    historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  }
  
  public AllProcessInstancesPage(String processInstanceId) {
    this();
    this.processInstanceId = processInstanceId;
  }

  protected LazyLoadingQuery createLazyLoadingQuery() {
    return new AllProcessInstancesListQuery(historyService, repositoryService);
  }
  

  protected UriFragment getUriFragment(String processInstanceId) {
    UriFragment fragment = new UriFragment(AllProcessesNavigator.ALL_PROCESSES_URI_PART);
    if(processInstanceId != null) {
      fragment.addUriPart(processInstanceId);
    }
    return fragment;
  }
  
}
