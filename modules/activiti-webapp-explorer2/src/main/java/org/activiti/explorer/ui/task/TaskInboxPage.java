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

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.data.LazyLoadingQuery;
import org.activiti.explorer.navigation.TaskNavigator;
import org.activiti.explorer.navigation.UriFragment;
import org.activiti.explorer.ui.custom.ListSearchBox;
import org.activiti.explorer.ui.task.data.TaskInboxListQuery;

import com.vaadin.ui.Component;




/**
 * The page displaying all tasks currently in ones inbox.
 * 
 * @author Joram Barrez
 */
public class TaskInboxPage extends TaskPage {
  
  private static final long serialVersionUID = 1L;
  
  protected String taskId;
  
  public TaskInboxPage() {
  }
  
  /**
   * Constructor called when page is accessed straight through the url, eg. /task/id=123
   */
  public TaskInboxPage(String taskId) {
    this.taskId = taskId;
  }
  
  @Override
  protected LazyLoadingQuery createLazyLoadingQuery() {
    return new TaskInboxListQuery();
  }
  
  @Override
  protected void initUi() {
    super.initUi();
    if (taskId == null) {
      selectListElement(0);
    } else {
      int index = taskListContainer.getIndexForObjectId(taskId);
      if(index > 0) {
        selectListElement(index);
      } else {
        ExplorerApp.get().getNotificationManager().showErrorNotification(Messages.TASK_AUTHORISATION_ERROR_TITLE, 
                ExplorerApp.get().getI18nManager().getMessage(Messages.TASK_AUTHORISATION_INBOX_ERROR, taskId));
        selectListElement(0);
      }
    }
  }

  @Override
  protected UriFragment getUriFragment(String taskId) {
    UriFragment taskFragment = new UriFragment(TaskNavigator.TASK_URI_PART);

    if(taskId != null) {
      taskFragment.addUriPart(taskId);
    }

    taskFragment.addParameter(TaskNavigator.PARAMETER_CATEGORY, TaskNavigator.CATEGORY_INBOX);
    return taskFragment;
  }
  
  @Override
  protected Component getSearchComponent() {
    ListSearchBox searchBox = new ListSearchBox();
    return searchBox;
  } 

  
}
