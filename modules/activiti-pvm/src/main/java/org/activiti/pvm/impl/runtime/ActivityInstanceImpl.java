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

package org.activiti.pvm.impl.runtime;

import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.runtime.PvmActivityInstance;


/**
 * @author Tom Baeyens
 */
public class ActivityInstanceImpl extends ScopeInstanceImpl implements PvmActivityInstance {

  protected ActivityImpl activity;
  protected ExecutionContextImpl executionContext;
  protected boolean isActive;
  
  protected ActivityInstanceImpl() {
  }
  
  public ActivityInstanceImpl(ActivityImpl activity, ScopeInstanceImpl parent) {
    super(parent.getProcessDefinition(), activity);
    this.activity = activity;
    this.parent = parent;
  }
  
  public void signal(String signalName, Object signalData) {
    ExecutionContextImpl.signal(this, signalName, signalData);
  }
  
  public String toString() {
    return "ActivityInstanceImpl["+System.identityHashCode(this)+"]";
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public ActivityImpl getActivity() {
    return activity;
  }

  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
  }

  public ExecutionContextImpl getExecutionContext() {
    return executionContext;
  }

  public void setExecutionContext(ExecutionContextImpl executionContext) {
    this.executionContext = executionContext;
  }
  
  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }
}
