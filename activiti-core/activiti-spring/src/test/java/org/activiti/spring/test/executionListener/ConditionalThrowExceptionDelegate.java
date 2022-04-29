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

package org.activiti.spring.test.executionListener;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;


public class ConditionalThrowExceptionDelegate implements JavaDelegate {

  private Expression injectedVar;

  @Override
  public void execute(DelegateExecution execution) {
    Object throwException = execution.getVariable(execution.getCurrentActivityId());

    if (throwException != null && (boolean) throwException) {
      throw new ActivitiException("throwException was true");
    }

    if (injectedVar != null && injectedVar.getValue(execution) != null) {
      execution.setVariable("injectedExecutionVariable", injectedVar.getValue(execution));
    }
  }
}
