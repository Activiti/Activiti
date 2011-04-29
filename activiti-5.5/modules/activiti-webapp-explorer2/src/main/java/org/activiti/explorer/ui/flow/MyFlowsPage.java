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

package org.activiti.explorer.ui.flow;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.MyFlowsNavigator;
import org.activiti.explorer.navigation.UriFragment;

import com.vaadin.ui.Component;


/**
 * @author Frederik Heremans
 */
public class MyFlowsPage extends ProcessInstancePage {

  private static final long serialVersionUID = 1L;

  protected String processInstanceId;
  
  protected RepositoryService repositoryService;
  protected HistoryService historyService;
  
  public MyFlowsPage() {
    historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  }
  
  public MyFlowsPage(String processInstanceId) {
    this();
    this.processInstanceId = processInstanceId;
  }

  @Override
  protected LazyLoadingQuery createLazyLoadingQuery() {
    return new MyFlowsListQuery(historyService, repositoryService);
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    
    if(processInstanceId != null) {
      selectListElement(processInstanceListContainer.getIndexForObjectId(processInstanceId));
    } else {
      selectListElement(0);
    }
  }

  @Override
  protected UriFragment getUriFragment(String processInstanceId) {
    UriFragment fragment = new UriFragment(MyFlowsNavigator.MY_FLOWS_URI_PART);
    if(processInstanceId != null) {
      fragment.addUriPart(processInstanceId);
    }
    return fragment;
  }
  
  @Override
  protected Component getEventComponent() {
    return null;
  } 

}
