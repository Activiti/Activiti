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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.MyFlowsNavigationHandler;
import org.activiti.explorer.navigation.UriFragment;

import com.vaadin.ui.Component;


/**
 * @author Frederik Heremans
 */
public class MyFlowsPage extends ProcessInstancePage {

  private static final long serialVersionUID = 8859037187001514376L;

  protected String processInstanceId;
  
  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  
  public MyFlowsPage() {
    runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
    repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
  }
  
  public MyFlowsPage(String processInstanceId) {
    this();
    this.processInstanceId = processInstanceId;
  }

  @Override
  protected LazyLoadingQuery createLazyLoadingQuery() {
    return new MyFlowsListQuery(runtimeService, repositoryService);
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
    UriFragment fragment = new UriFragment(MyFlowsNavigationHandler.MY_FLOWS_URI_PART);
    if(processInstanceId != null) {
      fragment.addUriPart(processInstanceId);
    }
    return fragment;
  }
  
  @Override
  protected Component getSearchComponent() {
    return null;
  } 
  
  @Override
  protected Component getEventComponent() {
    return null;
  } 

}
