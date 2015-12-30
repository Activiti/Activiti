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

package org.activiti5.engine.impl.bpmn.behavior;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti5.engine.ActivitiException;
import org.activiti5.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti5.engine.impl.bpmn.parser.BpmnParse;
import org.activiti5.engine.impl.context.Context;
import org.activiti5.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti5.engine.impl.pvm.PvmActivity;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti5.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.activiti5.engine.impl.pvm.process.ActivityImpl;


/**
 * Implementation of the BPMN 2.0 subprocess (formally known as 'embedded' subprocess):
 * a subprocess defined within another process definition.
 * 
 * @author Joram Barrez
 */
public class SubProcessActivityBehavior extends AbstractBpmnActivityBehavior implements CompositeActivityBehavior {
  
  public void execute(DelegateExecution execution) {
    ActivityExecution activityExecution = (ActivityExecution) execution;
    PvmActivity activity = activityExecution.getActivity();
    ActivityImpl initialActivity = (ActivityImpl) activity.getProperty(BpmnParse.PROPERTYNAME_INITIAL);
    
    if (initialActivity == null) {
      throw new ActivitiException("No initial activity found for subprocess " 
              + activityExecution.getActivity().getId());
    }

    // initialize the template-defined data objects as variables
    initializeDataObjects(activityExecution, activity);
    
    if (initialActivity.getActivityBehavior() != null 
          && initialActivity.getActivityBehavior() instanceof NoneStartEventActivityBehavior) { // embedded subprocess: only none start allowed
        ((ExecutionEntity) execution).setActivity(initialActivity);
        Context.getCommandContext().getHistoryManager().recordActivityStart((ExecutionEntity) execution);
    }

    activityExecution.executeActivity(initialActivity);
  }
  
  public void lastExecutionEnded(ActivityExecution execution) {
    ScopeUtil.createEventScopeExecution((ExecutionEntity) execution);
    
    // remove the template-defined data object variables
    Map<String, Object> dataObjectVars = ((ActivityImpl) execution.getActivity()).getVariables();
    if (dataObjectVars != null) {
      execution.removeVariablesLocal(dataObjectVars.keySet());
    }

    bpmnActivityBehavior.performDefaultOutgoingBehavior(execution);
  }

  protected void initializeDataObjects(ActivityExecution execution, PvmActivity activity) {
    Map<String, Object> dataObjectVars = ((ActivityImpl) activity).getVariables();
    if (dataObjectVars != null) {
      execution.setVariablesLocal(dataObjectVars);
    }
  }
}
