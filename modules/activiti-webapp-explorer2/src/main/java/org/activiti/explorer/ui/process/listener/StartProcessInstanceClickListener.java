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

package org.activiti.explorer.ui.process.listener;

import java.util.List;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ui.process.ProcessDefinitionPage;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class StartProcessInstanceClickListener implements ClickListener {

  private static final long serialVersionUID = 1L;
  
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected FormService formService;
  protected NotificationManager notificationManager;
  
  protected ProcessDefinition processDefinition;
  protected ProcessDefinitionPage parentPage;
  
  
  public StartProcessInstanceClickListener(ProcessDefinition processDefinition, ProcessDefinitionPage processDefinitionPage) {
    this.runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.formService = ProcessEngines.getDefaultProcessEngine().getFormService();
    this.notificationManager = ExplorerApp.get().getNotificationManager(); 
    
    this.processDefinition = processDefinition;
    this.parentPage = processDefinitionPage;
  }

  public void buttonClick(ClickEvent event) {
    // Check if process-definition defines a start-form
    
    StartFormData startFormData = formService.getStartFormData(processDefinition.getId());
    if(startFormData != null && ((startFormData.getFormProperties() != null && startFormData.getFormProperties().size() > 0) || startFormData.getFormKey() != null)) {
      parentPage.showStartForm(processDefinition, startFormData);
    } else {
      // Just start the process-instance since it has no form.
      // TODO: Error handling
      ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
      
      // Show notification of success
      notificationManager.showInformationNotification(Messages.PROCESS_STARTED_NOTIFICATION, getProcessDisplayName(processDefinition));
      
      // Switch to inbox page in case a task of this process was created
      List<Task> loggedInUsersTasks = taskService.createTaskQuery()
        .taskAssignee(ExplorerApp.get().getLoggedInUser().getId())
        .processInstanceId(processInstance.getId())
        .list();
      if (loggedInUsersTasks.size() > 0) {
        ExplorerApp.get().getViewManager().showInboxPage(loggedInUsersTasks.get(0).getId());
      }
    }
  }
  
  protected String getProcessDisplayName(ProcessDefinition processDefinition) {
    if(processDefinition.getName() != null) {
      return processDefinition.getName();
    } else {
      return processDefinition.getKey();
    }
  }

}
