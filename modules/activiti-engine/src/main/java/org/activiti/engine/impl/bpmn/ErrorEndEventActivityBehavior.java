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

import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.runtime.ExecutionEntity;


/**
 * @author Joram Barrez
 */
public class ErrorEndEventActivityBehavior extends AbstractBpmnActivity {
  
  protected static final Logger LOG = Logger.getLogger(ErrorEndEventActivityBehavior.class.getName());
  protected String catchingActivityId;
  protected String errorCode;
  
  public ErrorEndEventActivityBehavior(String errorCode) {
    this.errorCode = errorCode;
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    
    if (catchingActivityId == null) {
      LOG.info(execution.getActivity().getId() + " throws error event with errorCode '"
              + errorCode + "', but no catching boundary event was defined. "
              +		"Execution will simply be ended (none end event semantics).");
      execution.end();
    } else {
      ProcessDefinitionImpl processDefinition = ((ExecutionEntity) execution).getProcessDefinition();
      ActivityImpl catchingActivity = processDefinition.findActivity(catchingActivityId);
      if (catchingActivity == null) {
        throw new ActivitiException(catchingActivityId + " not found in process definition");
      }
      execution.executeActivity(catchingActivity);
    }
    
  }

  
  public String getCatchingActivityId() {
    return catchingActivityId;
  }

  public void setCatchingActivityId(String catchingActivityId) {
    this.catchingActivityId = catchingActivityId;
  }
  
  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }
  
}
