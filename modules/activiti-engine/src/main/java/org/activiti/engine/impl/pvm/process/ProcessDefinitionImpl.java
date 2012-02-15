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
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;



/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionImpl extends ScopeImpl implements PvmProcessDefinition {
  
  private static final long serialVersionUID = 1L;
  
  protected String name;
  protected String description;
  protected ActivityImpl initial;
  protected List<ActivityImpl> initialActivityStack;

  public ProcessDefinitionImpl(String id) {
    super(id, null);
    processDefinition = this;
  }

  public PvmProcessInstance createProcessInstance() {
    InterpretableExecution processInstance = newProcessInstance();
    processInstance.setProcessDefinition(this);
    processInstance.setProcessInstance(processInstance);
    processInstance.initialize();

    InterpretableExecution scopeInstance = processInstance;
    List<ActivityImpl> initialActivities = getInitialActivityStack();
    for (ActivityImpl initialActivity: initialActivities) {
      if (initialActivity.isScope()) {
        scopeInstance = (InterpretableExecution) scopeInstance.createExecution();
        scopeInstance.setActivity(initialActivity);
        if (initialActivity.isScope()) {
          scopeInstance.initialize();
        }
      }
    }
    
    scopeInstance.setActivity(initial);

    return processInstance;
  }
  
  /** creates a process instance using the provided activity as initial */
  public PvmProcessInstance createProcessInstanceForInitial(ActivityImpl startActivity) {
    InterpretableExecution processInstance = newProcessInstance();
    processInstance.setProcessDefinition(this);
    processInstance.setProcessInstance(processInstance);
    processInstance.initialize();

    InterpretableExecution scopeInstance = processInstance;
    
    ArrayList<ActivityImpl> initialActivityStack = new ArrayList<ActivityImpl>();
    ActivityImpl activity = startActivity;
    while (activity!=null) {
      initialActivityStack.add(0, activity);
      activity = activity.getParentActivity();
    }
    
    for (ActivityImpl initialActivity: initialActivityStack) {
      if (initialActivity.isScope()) {
        scopeInstance = (InterpretableExecution) scopeInstance.createExecution();
        scopeInstance.setActivity(initialActivity);
        if (initialActivity.isScope()) {
          scopeInstance.initialize();
        }
      }
    }
    
    scopeInstance.setActivity(startActivity);

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

  protected InterpretableExecution newProcessInstance() {
    return new ExecutionImpl();
  }

  public String getDiagramResourceName() {
    return null;
  }

  public String getDeploymentId() {
    return null;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getDescription() {
    return (String) getProperty("documentation");
  }
}
