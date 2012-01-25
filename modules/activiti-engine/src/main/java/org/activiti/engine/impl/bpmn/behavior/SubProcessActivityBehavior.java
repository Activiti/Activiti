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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;


/**
 * Implementation of the BPMN 2.0 subprocess (formely know as 'embedded' subprocess):
 * a subprocess defined within another process definition.
 * 
 * @author Joram Barrez
 */
public class SubProcessActivityBehavior extends AbstractBpmnActivityBehavior implements CompositeActivityBehavior {
  
  public void execute(ActivityExecution execution) throws Exception {
    PvmActivity activity = execution.getActivity();
    ActivityImpl initialActivity = (ActivityImpl) activity.getProperty(BpmnParse.PROPERTYNAME_INITIAL);
    
    if (initialActivity == null) {
      throw new ActivitiException("No initial activity found for subprocess " 
              + execution.getActivity().getId());
    }
    
    execution.executeActivity(initialActivity);
  }
  
  public void lastExecutionEnded(ActivityExecution execution) {
    bpmnActivityBehavior.performDefaultOutgoingBehavior(execution);
  }

}
