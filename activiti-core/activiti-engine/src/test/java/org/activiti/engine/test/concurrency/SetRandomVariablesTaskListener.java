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


package org.activiti.engine.test.concurrency;

import java.util.Random;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * Tasklistener that sets some random process and task-variables.
 *

 */
public class SetRandomVariablesTaskListener implements TaskListener {

  private static final long serialVersionUID = 1L;

  @Override
  public void notify(DelegateTask delegateTask) {
    String varName;
    for (int i = 0; i < 5; i++) {
      varName = "variable-" + new Random().nextInt(10);
      delegateTask.getExecution().setVariable(varName, getRandomValue());
    }

    for (int i = 0; i < 5; i++) {
      varName = "task-variable-" + new Random().nextInt(10);
      delegateTask.setVariableLocal(varName, getRandomValue());
    }
  }

  protected Object getRandomValue() {
    switch (new Random().nextInt(4)) {
    case 0:
      return new Random().nextLong();
    case 1:
      return new Random().nextDouble();
    case 2:
      return "Activiti is a light-weight workflow and Business Process Management (BPM) Platform";
    default:
      return new Random().nextBoolean();
      // return "Some bytearray".getBytes();
    }
  }

}
