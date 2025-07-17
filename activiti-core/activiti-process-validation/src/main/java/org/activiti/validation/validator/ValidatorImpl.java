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

package org.activiti.validation.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.validation.ValidationError;

public abstract class ValidatorImpl implements Validator {

  public void addError(List<ValidationError> validationErrors, ValidationError error) {
    validationErrors.add(error);
  }

  protected void addError(List<ValidationError> validationErrors, String problem) {
    addError(validationErrors, problem, null, null, false);
  }

  protected void addError(List<ValidationError> validationErrors, String problem, Map<String, String> params) {
    addError(validationErrors, problem, null, null, false, params);
  }

  protected void addError(List<ValidationError> validationErrors, String problem,
      BaseElement baseElement) {
    addError(validationErrors, problem, null, baseElement);
  }

  protected void addError(List<ValidationError> validationErrors, String problem,
      BaseElement baseElement, Map<String, String> params) {
    addError(validationErrors, problem, null, baseElement, params);
  }

  protected void addError(List<ValidationError> validationErrors, String problem, Process process,
      BaseElement baseElement) {
    addError(validationErrors, problem, process, baseElement, false);
  }

  protected void addError(List<ValidationError> validationErrors, String problem, Process process,
      BaseElement baseElement, Map<String, String> params) {
    addError(validationErrors, problem, process, baseElement, false, params);
  }

  protected void addWarning(List<ValidationError> validationErrors, String problem, Process process,
      BaseElement baseElement) {
    addError(validationErrors, problem, process, baseElement, true);
  }

  protected void addWarning(List<ValidationError> validationErrors, String problem, Process process,
      BaseElement baseElement, Map<String, String> params) {
    addError(validationErrors, problem, process, baseElement, true, params);
  }

  protected void addError(List<ValidationError> validationErrors, String problem, Process process,
      BaseElement baseElement, boolean isWarning) {
    addError(validationErrors, problem, process, baseElement, isWarning, new HashMap<>());
  }

  protected void addError(List<ValidationError> validationErrors, String problem, Process process,
      BaseElement baseElement, boolean isWarning, Map<String, String> params) {
    ValidationError error = new ValidationError();
    error.setWarning(isWarning);

    if (process != null) {
      error.setProcessDefinitionId(process.getId());
      error.setProcessDefinitionName(process.getName());
    }

    if (baseElement != null) {
      error.setXmlLineNumber(baseElement.getXmlRowNumber());
      error.setXmlColumnNumber(baseElement.getXmlColumnNumber());
    }
    error.setKey(problem);
    error.setProblem(problem);
    error.setDefaultDescription(problem);
    error.setParams(params);

    if (baseElement instanceof FlowElement) {
      FlowElement flowElement = (FlowElement) baseElement;
      error.setActivityId(flowElement.getId());
      error.setActivityName(flowElement.getName());
    }

    addError(validationErrors, error);
  }

  protected void addError(List<ValidationError> validationErrors, String problem, Process process,
      String id) {
    ValidationError error = new ValidationError();

    if (process != null) {
      error.setProcessDefinitionId(process.getId());
      error.setProcessDefinitionName(process.getName());
    }

    error.setKey(problem);
    error.setProblem(problem);
    error.setDefaultDescription(problem);
    error.setActivityId(id);

    addError(validationErrors, error);
  }

}
