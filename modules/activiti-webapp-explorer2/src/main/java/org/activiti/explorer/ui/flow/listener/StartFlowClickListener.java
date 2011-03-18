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

package org.activiti.explorer.ui.flow.listener;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.explorer.ExplorerApplication;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Frederik Heremans
 */
public class StartFlowClickListener implements ClickListener {

  private static final long serialVersionUID = -1811557526259754226L;
  
  protected ProcessDefinition processDefinition;
  protected RuntimeService runtimeService;
  protected FormService formService;
  
  
  public StartFlowClickListener(ProcessDefinition processDefinition) {
    this.runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
    this.formService = ProcessEngines.getDefaultProcessEngine().getFormService();
    this.processDefinition = processDefinition;
  }

  public void buttonClick(ClickEvent event) {
    // Check if process-definition defines a start-form
    
    StartFormData data = formService.getStartFormData(processDefinition.getId());
    if(data != null && ((data.getFormProperties() != null && data.getFormProperties().size() > 0) || data.getFormKey() != null)) {
      // TODO: Render form based on form-properties
    } else {
      // Just start the process-instance since it has no form.
      // TODO: Error handling
      ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    }
    
    // Show notification of success
    ExplorerApplication.getCurrent().getMainWindow().showNotification("Process '" + processDefinition.getName() + "' has been started");
  }

}
