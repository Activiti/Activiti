/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.delegate;

import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * 
 * @author Daniel Meyer
 */
public class ActivityBehaviorInvocation extends DelegateInvocation {

  protected final ActivityBehavior behaviorInstance;

  protected final ActivityExecution execution;

  public ActivityBehaviorInvocation(ActivityBehavior behaviorInstance, ActivityExecution execution) {
    this.behaviorInstance = behaviorInstance;
    this.execution = execution;
  }

  protected void invoke() throws Exception {
    behaviorInstance.execute(execution);
  }

  public Object getTarget() {
    return behaviorInstance;
  }

}
