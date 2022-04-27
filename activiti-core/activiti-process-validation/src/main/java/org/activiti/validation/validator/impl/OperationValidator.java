/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Interface;
import org.activiti.bpmn.model.Operation;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ValidatorImpl;


public class OperationValidator extends ValidatorImpl {

  @Override
  public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
    if (bpmnModel.getInterfaces() != null) {
      for (Interface bpmnInterface : bpmnModel.getInterfaces()) {
        if (bpmnInterface.getOperations() != null) {
          for (Operation operation : bpmnInterface.getOperations()) {
            if (bpmnModel.getMessage(operation.getInMessageRef()) == null) {
              addError(errors, Problems.OPERATION_INVALID_IN_MESSAGE_REFERENCE, null, operation, "Invalid inMessageRef for operation");
            }
          }
        }
      }
    }
  }

}
