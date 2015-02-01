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
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.MyProcessesNavigator;
import org.activiti.explorer.navigation.UriFragment;


/**
 * @author Frederik Heremans
 */
public class MyProcessInstancesPage extends ProcessInstancePage {

  private static final long serialVersionUID = 1L;

  protected String processInstanceId;
  
  protected transient RepositoryService repositoryService;
  protected transient HistoryService historyService;
  
  public MyProcessInstancesPage() {
    historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  }
  
  public MyProcessInstancesPage(String processInstanceId) {
    this();
    this.processInstanceId = processInstanceId;
  }

  @Override
  protected LazyLoadingQuery createLazyLoadingQuery() {
    return new MyProcessInstancesListQuery(historyService, repositoryService);
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    
    if(processInstanceId != null) {
      selectElement(processInstanceListContainer.getIndexForObjectId(processInstanceId));
    } else {
      selectElement(0);
    }
  }

  @Override
  protected UriFragment getUriFragment(String processInstanceId) {
    UriFragment fragment = new UriFragment(MyProcessesNavigator.MY_PROCESSES_URI_PART);
    if(processInstanceId != null) {
      fragment.addUriPart(processInstanceId);
    }
    return fragment;
  }
  
}
