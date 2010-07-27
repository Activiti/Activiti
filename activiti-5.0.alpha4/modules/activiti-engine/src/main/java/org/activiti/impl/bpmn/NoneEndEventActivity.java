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
package org.activiti.impl.bpmn;

import org.activiti.impl.definition.ActivityImpl;
import org.activiti.pvm.ActivityExecution;


/**
 * @author Joram Barrez
 */
public class NoneEndEventActivity extends BpmnActivity {
  
  public void execute(ActivityExecution execution) throws Exception {
    
    // TODO: needs cleanup!
    ActivityImpl currentActivity = (ActivityImpl) execution.getActivity();
    ActivityImpl parentActivity = (ActivityImpl) currentActivity.getParentActivity();
    
    if (parentActivity != null &&
            parentActivity.getActivityBehavior() instanceof SubProcessActivity) {
      
      // No need to end the execution, see ExecutionImpl.destroyScope
      execution.setActivity(parentActivity);
      leave(execution);
      
    } else {
      
      // Need to locally store the parent, since end() will remove the child-parent relation
      ActivityExecution parent = execution.getParent();

      execution.end();
      
      // Special case for BPMN 2.0: when the parent is a process instance, 
      // but is not more active and has no children anymore
      // Then the process instance cannot continue anymore:
      //
      // eg. start -> fork -> task1 -> end1
      //                   -> task2 -> end2
      if (parent != null
              && parent.isProcessInstance() 
              && parent.getExecutions().isEmpty()
              && !parent.isActive()) {
        parent.end();
      }
    }
    
  }

}
