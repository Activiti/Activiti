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

package org.activiti.pvm.impl.process;

import org.activiti.pvm.impl.runtime.ProcessInstanceImpl;
import org.activiti.pvm.process.PvmProcessDefinition;



/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionImpl extends ScopeImpl implements PvmProcessDefinition {
  
  private static final long serialVersionUID = 1L;
  
  protected ActivityImpl initial;

  public ProcessDefinitionImpl(String id) {
    super(id, null);
    processDefinition = this;
  }

  public ProcessInstanceImpl createProcessInstance() {
    return new ProcessInstanceImpl(this);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public ActivityImpl getInitial() {
    return initial;
  }

  public void setInitial(ActivityImpl initial) {
    this.initial = initial;
  }
  
  public String toString() {
    return "ProcessDefinitionImpl["+System.identityHashCode(this)+"]";
  }
}
