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
package org.activiti.form.engine.impl;

import java.util.Map;

import org.activiti.form.api.FormService;
import org.activiti.form.api.SubmittedForm;
import org.activiti.form.api.SubmittedFormQuery;
import org.activiti.form.engine.impl.cmd.GetCompletedFormDefinitionCmd;
import org.activiti.form.engine.impl.cmd.GetRuntimeFormDefinitionCmd;
import org.activiti.form.engine.impl.cmd.GetVariablesFromFormSubmissionCmd;
import org.activiti.form.engine.impl.cmd.StoreSubmittedFormCmd;
import org.activiti.form.model.CompletedFormDefinition;
import org.activiti.form.model.FormDefinition;

/**
 * @author Tijs Rademakers
 */
public class FormServiceImpl extends ServiceImpl implements FormService {

  public void completeForm(FormDefinition formDefinition) {

  }
  
  public Map<String, Object> getVariablesFromFormSubmission(FormDefinition formDefinition, Map<String, Object> values) {
    return commandExecutor.execute(new GetVariablesFromFormSubmissionCmd(formDefinition, values));
  }

  public Map<String, Object> getVariablesFromFormSubmission(FormDefinition formDefinition, Map<String, Object> values, String outcome) {
    return commandExecutor.execute(new GetVariablesFromFormSubmissionCmd(formDefinition, values, outcome));
  }
  
  public SubmittedForm storeSubmittedForm(Map<String, Object> variables, FormDefinition formDefinition, String taskId, String processInstanceId) {
    return commandExecutor.execute(new StoreSubmittedFormCmd(formDefinition, variables, taskId, processInstanceId));
  }
  
  public FormDefinition getTaskFormDefinitionById(String formId, String processInstanceId, Map<String, Object> variables) {
    return commandExecutor.execute(new GetRuntimeFormDefinitionCmd(null, formId, processInstanceId, variables));
  }
  
  public FormDefinition getTaskFormDefinitionById(String formId, String processInstanceId, 
      Map<String, Object> variables, String tenantId) {
    return commandExecutor.execute(new GetRuntimeFormDefinitionCmd(null, formId, processInstanceId, tenantId, variables));
  }
  
  public FormDefinition getTaskFormDefinitionByKey(String formDefinitionKey, String processInstanceId, Map<String, Object> variables) {
    return commandExecutor.execute(new GetRuntimeFormDefinitionCmd(formDefinitionKey, null, processInstanceId, variables));
  }
  
  public FormDefinition getTaskFormDefinitionByKey(String formDefinitionKey, String processInstanceId, 
      Map<String, Object> variables, String tenantId) {
    
    return commandExecutor.execute(new GetRuntimeFormDefinitionCmd(formDefinitionKey, null, null, processInstanceId, tenantId, variables));
  }
  
  public FormDefinition getTaskFormDefinitionByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId, 
      String processInstanceId, Map<String, Object> variables) {
    
    return commandExecutor.execute(new GetRuntimeFormDefinitionCmd(formDefinitionKey, parentDeploymentId, null, processInstanceId, variables));
  }
  
  public FormDefinition getTaskFormDefinitionByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId, String processInstanceId, 
      Map<String, Object> variables, String tenantId) {
    
    return commandExecutor.execute(new GetRuntimeFormDefinitionCmd(formDefinitionKey, parentDeploymentId, null, processInstanceId, tenantId, variables));
  }
  
  public CompletedFormDefinition getCompletedTaskFormDefinitionById(String formId, String taskId, String processInstanceId, Map<String, Object> variables) {
    return commandExecutor.execute(new GetCompletedFormDefinitionCmd(null, formId, taskId, processInstanceId, variables));
  }
  
  public CompletedFormDefinition getCompletedTaskFormDefinitionById(String formId, String taskId, String processInstanceId, 
      Map<String, Object> variables, String tenantId) {
    return commandExecutor.execute(new GetCompletedFormDefinitionCmd(null, formId, taskId, processInstanceId, tenantId, variables));
  }
  
  public CompletedFormDefinition getCompletedTaskFormDefinitionByKey(String formDefinitionKey, String taskId, String processInstanceId, Map<String, Object> variables) {
    return commandExecutor.execute(new GetCompletedFormDefinitionCmd(formDefinitionKey, null, taskId, processInstanceId, variables));
  }
  
  public CompletedFormDefinition getCompletedTaskFormDefinitionByKey(String formDefinitionKey, String taskId, String processInstanceId, 
      Map<String, Object> variables, String tenantId) {
    
    return commandExecutor.execute(new GetCompletedFormDefinitionCmd(formDefinitionKey, null, null, taskId, processInstanceId, tenantId, variables));
  }
  
  public CompletedFormDefinition getCompletedTaskFormDefinitionByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId,
      String taskId, String processInstanceId, Map<String, Object> variables) {
    
    return commandExecutor.execute(new GetCompletedFormDefinitionCmd(formDefinitionKey, parentDeploymentId, null, taskId, processInstanceId, variables));
  }
  
  public CompletedFormDefinition getCompletedTaskFormDefinitionByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId,
      String taskId, String processInstanceId, Map<String, Object> variables, String tenantId) {
    
    return commandExecutor.execute(new GetCompletedFormDefinitionCmd(formDefinitionKey, parentDeploymentId, null, 
        taskId, processInstanceId, tenantId, variables));
  }
  
  public SubmittedFormQuery createSubmittedFormQuery() {
    return new SubmittedFormQueryImpl(commandExecutor);
  }
}
