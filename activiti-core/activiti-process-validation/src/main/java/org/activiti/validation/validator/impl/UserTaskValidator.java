/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.UserTask;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;


public class UserTaskValidator extends ProcessLevelValidator {

  @Override
  protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
    List<UserTask> userTasks = process.findFlowElementsOfType(UserTask.class);
    for (UserTask userTask : userTasks) {
      if (userTask.getTaskListeners() != null) {
        for (ActivitiListener listener : userTask.getTaskListeners()) {
          if (listener.getImplementation() == null || listener.getImplementationType() == null) {
            addError(errors, Problems.USER_TASK_LISTENER_IMPLEMENTATION_MISSING, process, userTask, "Element 'class' or 'expression' is mandatory on executionListener");
          }
        }
      }
    }
  }

}
