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

import org.activiti.form.engine.FormService;
import org.activiti.form.engine.impl.cmd.GetVariablesFromFormSubmissionCmd;
import org.activiti.form.engine.impl.cmd.StoreSubmittedFormCmd;
import org.activiti.form.engine.repository.SubmittedForm;
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
}
