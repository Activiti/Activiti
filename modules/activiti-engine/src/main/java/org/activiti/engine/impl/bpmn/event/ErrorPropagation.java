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

package org.activiti.engine.impl.bpmn.event;

import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmScope;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;


/**
 * @author Falko Menge
 */
public class ErrorPropagation {

  public static void propagateError(ActivityExecution execution, BpmnError error) throws Exception {
    // find local error handler
    PvmActivity errorEventHandler = null;
    PvmScope scope = execution.getActivity();
    while (errorEventHandler == null && scope != null) {
      // search for error handler with same error code as thrown Error
      for (PvmActivity activity : scope.getActivities()) {
        if (((ActivityImpl) activity).getActivityBehavior() instanceof BoundaryEventActivityBehavior
                && error.getErrorCode().equals(activity.getProperty("errorCode"))) {
          errorEventHandler = activity;
          break;
        }
      }
      // search for generic error handler if no error handler with that error code has been found
      if (errorEventHandler == null) {
        for (PvmActivity activity : scope.getActivities()) {
          if (((ActivityImpl) activity).getActivityBehavior() instanceof BoundaryEventActivityBehavior
                  && (activity.getProperty("errorCode") == null || "".equals(activity.getProperty("errorCode")))) {
            errorEventHandler = activity;
            break;
          }
        }
        
      }
      // search for error handlers in parent scopes 
      if (errorEventHandler == null) {
        if (scope instanceof PvmActivity) {
          scope = ((PvmActivity) scope).getParent();
        } else {
          scope = null; // stop search
        }
      }
    }
    
    ErrorEndEventActivityBehavior errorEndEvent = new ErrorEndEventActivityBehavior(error.getErrorCode());
    if (errorEventHandler != null) {
      errorEndEvent.setBorderEventActivityId(errorEventHandler.getId());
    }
    // execute error handler
    errorEndEvent.execute(execution);
  }

}
