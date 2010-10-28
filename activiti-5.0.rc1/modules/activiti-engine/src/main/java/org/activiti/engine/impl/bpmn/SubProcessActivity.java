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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;


/**
 * Implementation of the BPMN 2.0 subprocess (formely know as 'embedded' subprocess):
 * a subprocess defined within another process definition.
 * 
 * @author Joram Barrez
 */
public class SubProcessActivity extends AbstractBpmnActivity implements CompositeActivityBehavior {
  
  public void execute(ActivityExecution execution) throws Exception {
    PvmActivity activity = execution.getActivity();
    ActivityImpl initialActivity = (ActivityImpl) activity.getProperty(BpmnParse.PROPERTYNAME_INITIAL);

    execution.executeActivity(initialActivity);
  }
  
  public void lastExecutionEnded(ActivityExecution execution) {
    List<PvmTransition> outgoingTransitions = execution.getActivity().getOutgoingTransitions();
    execution.takeAll(outgoingTransitions, new ArrayList<ActivityExecution>());
  }

}
