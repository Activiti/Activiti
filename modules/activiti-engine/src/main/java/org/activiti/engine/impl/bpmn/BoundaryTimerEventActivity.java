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
package org.activiti.engine.impl.bpmn;

import org.activiti.engine.ActivitiException;
import org.activiti.pvm.activity.ActivityContext;


/**
 * implementation of the boundary timer event logic.
 * 
 * @author Joram Barrez
 */
public class BoundaryTimerEventActivity extends AbstractBpmnActivity {
  
  protected boolean interrupting;
    
  public void start(ActivityContext activityContext) throws Exception {
    
    if (interrupting) {
      leave(activityContext);
    } else {
      throw new ActivitiException("Non-interrupting boundary timer event not yet implemented");
    }
  }

  public boolean isInterrupting() {
    return interrupting;
  }

  public void setInterrupting(boolean interrupting) {
    this.interrupting = interrupting;
  }
  
}
