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

import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Signal;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;


public class SignalValidator extends ValidatorImpl {

  @Override
  public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
    Collection<Signal> signals = bpmnModel.getSignals();
    if (signals != null && !signals.isEmpty()) {

      for (Signal signal : signals) {
        if (StringUtils.isEmpty(signal.getId())) {
          addError(errors, Problems.SIGNAL_MISSING_ID, signal, "Signal must have an id");
        }

        if (StringUtils.isEmpty(signal.getName())) {
          addError(errors, Problems.SIGNAL_MISSING_NAME, signal, "Signal must have a name");
        }

        if (!StringUtils.isEmpty(signal.getName()) && duplicateName(signals, signal.getId(), signal.getName())) {
          addError(errors, Problems.SIGNAL_DUPLICATE_NAME, signal, "Duplicate signal name found");
        }

        if (signal.getScope() != null && !signal.getScope().equals(Signal.SCOPE_GLOBAL) && !signal.getScope().equals(Signal.SCOPE_PROCESS_INSTANCE)) {
          addError(errors, Problems.SIGNAL_INVALID_SCOPE, signal, "Invalid value for 'scope'. Only values 'global' and 'processInstance' are supported");
        }
      }

    }
  }

  protected boolean duplicateName(Collection<Signal> signals, String id, String name) {
    for (Signal signal : signals) {
      if (id != null && signal.getId() != null) {
        if (name.equals(signal.getName()) && !id.equals(signal.getId())) {
          return true;
        }
      }
    }
    return false;
  }

}
