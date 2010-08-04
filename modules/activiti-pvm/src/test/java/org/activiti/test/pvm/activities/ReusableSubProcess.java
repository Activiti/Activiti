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

package org.activiti.test.pvm.activities;

import org.activiti.pvm.activity.ActivityContext;
import org.activiti.pvm.activity.SubProcessActivityBehavior;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.process.PvmTransition;
import org.activiti.pvm.runtime.PvmProcessInstance;


/**
 * @author Tom Baeyens
 */
public class ReusableSubProcess implements SubProcessActivityBehavior {

  public void start(ActivityContext activityContext) throws Exception {
    PvmProcessDefinition processDefinition = null;
    PvmProcessInstance subProcessInstance = activityContext.createSubProcessInstance(processDefinition);
    
    // inject information from the super process into the subprocess
//    for (variableDeclarations) {
//      subProcessInstance.setVariable(null, null);
//    }
    
    subProcessInstance.start();
  }

  public void subProcessEnded(ActivityContext activityContext) throws Exception {
    // extract information from the subprocess and inject it into the superprocess
//    for (variableDeclarations) {
//      subProcessInstance.setVariable(null, null);
//    }
    
    // take default transition
    PvmTransition transition = activityContext.getActivity().getOutgoingTransitions().get(0);
    activityContext.take(transition);
  }
}
