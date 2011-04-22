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
package org.activiti.explorer.ui.task;

import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.TaskNavigator;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.task.data.QueuedListQuery;

import com.vaadin.ui.Component;

/**
 * Page showing all the queued tasks of one person.
 * 
 * @author Joram Barrez
 */
public class QueuedPage extends TaskPage {

  private static final long serialVersionUID = 1L;
  
  protected String groupId;
  
  public QueuedPage(String groupId) {
    this.groupId = groupId;
  }
  
  public QueuedPage(String groupId, String taskId) {
    super(taskId);
    this.groupId = groupId;
  }
  
  @Override
  protected LazyLoadingQuery createLazyLoadingQuery() {
    return new QueuedListQuery(groupId);
  }
  
  @Override
  protected UriFragment getUriFragment(String taskId) {
    UriFragment taskFragment = new UriFragment(TaskNavigator.TASK_URI_PART);
    if(taskId != null) {
      taskFragment.addUriPart(taskId);
    }
    
    taskFragment.addParameter(TaskNavigator.PARAMETER_CATEGORY, TaskNavigator.CATEGORY_QUEUED);
    
    if(groupId != null) {
      taskFragment.addParameter(TaskNavigator.PARAMETER_GROUP, groupId);
    }
    return taskFragment;
  }
  
}
