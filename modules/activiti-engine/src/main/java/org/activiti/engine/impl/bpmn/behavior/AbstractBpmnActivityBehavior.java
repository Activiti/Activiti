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

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


/**
 * Denotes an 'activity' in the sense of BPMN 2.0: 
 * a parent class for all tasks, subprocess and callActivity. 
 * 
 * @author Joram Barrez
 */
public class AbstractBpmnActivityBehavior extends FlowNodeActivityBehavior {
  
  protected MultiInstanceActivityBehavior multiInstanceActivityBehavior;
  
  /**
   * Subclasses that call leave() will first pass through this method, before
   * the regular {@link FlowNodeActivityBehavior#leave(ActivityExecution)} is
   * called. This way, we can check if the activity has loop characteristics,
   * and delegate to the behavior if this is the case.
   */
  protected void leave(ActivityExecution execution) {
    if (!hasLoopCharacteristics()) {
      super.leave(execution);
    } else if (hasMultiInstanceCharacteristics()){
      multiInstanceActivityBehavior.leave(execution);
    }
  }
  
  protected boolean hasLoopCharacteristics() {
    return hasMultiInstanceCharacteristics();
  }
  
  protected boolean hasMultiInstanceCharacteristics() {
    return multiInstanceActivityBehavior != null;
  }

  public MultiInstanceActivityBehavior getMultiInstanceActivityBehavior() {
    return multiInstanceActivityBehavior;
  }
  
  public void setMultiInstanceActivityBehavior(MultiInstanceActivityBehavior multiInstanceActivityBehavior) {
    this.multiInstanceActivityBehavior = multiInstanceActivityBehavior;
  }

}
