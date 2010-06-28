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

package org.activiti.pvm;

import java.util.List;

import org.activiti.impl.execution.ExecutionImpl;


/**
 * @author Joram Barrez
 */
public class ExecutionControllerImpl implements ExecutionController {
  
  protected ExecutionImpl execution;
  
  protected ExecutionImpl concurrencyController;
  
  public ExecutionControllerImpl(ExecutionImpl execution) {
    this.execution = execution;
    
    if (execution.isConcurrencyScope()) {
      this.concurrencyController = execution;
    } else {
      this.concurrencyController = execution.getParent();
    }
  }

  public ActivityExecution createExecution() {
    return concurrencyController.createExecution();
  }

  public List< ? extends ActivityExecution> getActiveExecutions() {
    return concurrencyController.getActiveExecutions();
  }
  
  public List< ? extends ActivityExecution> getExecutions() {
    return concurrencyController.getExecutions();
  }
  
  public void end() {
    execution.end();
  }

  public void setActive(boolean isActive) {
    execution.setActive(isActive);
  }

  public void setActivity(Activity activity) {
    execution.setActivity(activity);
  }

}
