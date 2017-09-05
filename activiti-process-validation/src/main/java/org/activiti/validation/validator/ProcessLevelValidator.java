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
package org.activiti.validation.validator;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.validation.ValidationError;

/**

 */
public abstract class ProcessLevelValidator extends ValidatorImpl {

  @Override
  public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
    for (Process process : bpmnModel.getProcesses()) {
      executeValidation(bpmnModel, process, errors);
    }
  }

  protected abstract void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors);

}
