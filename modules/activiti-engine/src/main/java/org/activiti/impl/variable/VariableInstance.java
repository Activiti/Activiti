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
package org.activiti.impl.variable;

import java.io.Serializable;

import org.activiti.ActivitiException;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.execution.ScopeInstanceImpl;
import org.activiti.impl.task.TaskImpl;

/**
 * @author Tom Baeyens
 */
public abstract class VariableInstance implements Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String name;
  protected ExecutionImpl processInstance;
  protected ExecutionImpl execution;
  protected TaskImpl task;
  protected String text;
  
  public VariableInstance() {
  }

  public abstract String getTypeName();
  public abstract boolean isAbleToStore(Object value);
  public abstract void setValue(Object value);
  public abstract Object getValue();

  public String getText() {
    return text;
  }
  public String getName() {
    return name;
  }
  public ExecutionImpl getProcessInstance() {
    return processInstance;
  }
  public ExecutionImpl getExecution() {
    return execution;
  }
  public TaskImpl getTask() {
    return task;
  }

  public void setScopeInstance(ScopeInstanceImpl scopeInstance) {
    if (scopeInstance==null) {
      this.execution = null;
      this.processInstance = null;
      this.task = null;
    } else if (scopeInstance instanceof ExecutionImpl) {
      this.execution = (ExecutionImpl) scopeInstance;
      this.processInstance = execution.getProcessInstance();
    } else if (scopeInstance instanceof TaskImpl) {
      this.task = (TaskImpl) scopeInstance;
    } else {
      throw new ActivitiException("unsupported scope instance type: "+scopeInstance);
    }
  }

}
