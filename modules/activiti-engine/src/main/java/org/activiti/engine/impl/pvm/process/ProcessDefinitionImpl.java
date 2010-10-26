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

package org.activiti.engine.impl.pvm.process;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;



/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionImpl extends ScopeImpl implements PvmProcessDefinition {
  
  private static final long serialVersionUID = 1L;
  
  protected ActivityImpl initial;
  protected List<ActivityImpl> initialActivityStack;

  public ProcessDefinitionImpl(String id) {
    super(id, null);
    processDefinition = this;
  }

  public PvmProcessInstance createProcessInstance() {
    ExecutionImpl processInstance = newProcessInstance();
    processInstance.setProcessDefinition(this);
    processInstance.setProcessInstance(processInstance);
    processInstance.initialize();

    ExecutionImpl scopeInstance = processInstance;
    List<ActivityImpl> initialActivities = getInitialActivityStack();
    for (ActivityImpl initialActivity: initialActivities) {
      if (initialActivity.isScope()) {
        scopeInstance = scopeInstance.createExecution();
        scopeInstance.setActivity(initialActivity);
        if (initialActivity.isScope()) {
          scopeInstance.initialize();
        }
      }
    }
    
    scopeInstance.setActivity(initial);

    return processInstance;
  }

  public synchronized List<ActivityImpl> getInitialActivityStack() {
    if (initialActivityStack==null) {
      initialActivityStack = new ArrayList<ActivityImpl>();
      ActivityImpl activity = initial;
      while (activity!=null) {
        initialActivityStack.add(0, activity);
        activity = activity.getParentActivity();
      }
    }
    return initialActivityStack;
  }

  protected ExecutionImpl newProcessInstance() {
    return new ExecutionImpl();
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public ActivityImpl getInitial() {
    return initial;
  }

  public void setInitial(ActivityImpl initial) {
    this.initial = initial;
  }
  
  public String toString() {
    return "ProcessDefinition("+id+")";
  }
}
