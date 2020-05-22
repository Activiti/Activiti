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
package org.activiti.examples.bpmn.executionlistener;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateExecution;

public class CustomFlowBean implements Serializable {

  private static final long serialVersionUID = 1L;

  public boolean executeLogic(String flowId, DelegateExecution execution) {
    Object conditionsObject = execution.getVariable(flowId + "_activiti_conditions");
    return conditionsObject != null;
  }
}
