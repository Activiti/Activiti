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

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Message;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;


public class MessageValidator extends ValidatorImpl {

  @Override
  public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
    if (bpmnModel.getMessages() != null && !bpmnModel.getMessages().isEmpty()) {
      for (Message message : bpmnModel.getMessages()) {

        // Item ref
        if (StringUtils.isNotEmpty(message.getItemRef())) {
          if (!bpmnModel.getItemDefinitions().containsKey(message.getItemRef())) {
            addError(errors, Problems.MESSAGE_INVALID_ITEM_REF, null, message, "Item reference is invalid: not found");
          }
        }

      }
    }
  }

}
