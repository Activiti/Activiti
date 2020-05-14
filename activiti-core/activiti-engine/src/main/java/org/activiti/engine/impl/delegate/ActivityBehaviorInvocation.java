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

package org.activiti.engine.impl.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.invocation.DelegateInvocation;


/**
 *

 */
public class ActivityBehaviorInvocation extends DelegateInvocation {

  protected final ActivityBehavior behaviorInstance;

  protected final DelegateExecution execution;

  public ActivityBehaviorInvocation(ActivityBehavior behaviorInstance, DelegateExecution execution) {
    this.behaviorInstance = behaviorInstance;
    this.execution = execution;
  }

  protected void invoke() {
    behaviorInstance.execute(execution);
  }

  public Object getTarget() {
    return behaviorInstance;
  }

}
