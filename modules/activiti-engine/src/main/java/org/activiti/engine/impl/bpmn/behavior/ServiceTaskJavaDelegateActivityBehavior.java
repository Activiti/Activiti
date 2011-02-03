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

package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.ExecutionListener;
import org.activiti.engine.impl.pvm.delegate.ExecutionListenerExecution;


/**
 * @author Tom Baeyens
 */
public class ServiceTaskJavaDelegateActivityBehavior extends BpmnActivityBehavior implements ActivityBehavior, ExecutionListener {
  
  protected JavaDelegate javaDelegate;
  
  protected ServiceTaskJavaDelegateActivityBehavior() {
  }

  public ServiceTaskJavaDelegateActivityBehavior(JavaDelegate javaDelegate) {
    this.javaDelegate = javaDelegate;
  }

  public void execute(ActivityExecution execution) throws Exception {
    execute((DelegateExecution) execution);
    performDefaultOutgoingBehavior(execution);
  }
  
  public void notify(ExecutionListenerExecution execution) throws Exception {
    execute((DelegateExecution) execution);
  }
  
  public void execute(DelegateExecution execution) throws Exception {
    javaDelegate.execute(execution);
  }
}
