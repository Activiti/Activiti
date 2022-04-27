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
package org.activiti.engine.impl.cmd;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ValidationError;


public class ValidateBpmnModelCmd implements Command<List<ValidationError>> {

  protected BpmnModel bpmnModel;

  public ValidateBpmnModelCmd(BpmnModel bpmnModel) {
    this.bpmnModel = bpmnModel;
  }

  @Override
  public List<ValidationError> execute(CommandContext commandContext) {
    ProcessValidator processValidator = commandContext.getProcessEngineConfiguration().getProcessValidator();
    if (processValidator == null) {
      throw new ActivitiException("No process validator defined");
    }

    return processValidator.validate(bpmnModel);
  }

}
