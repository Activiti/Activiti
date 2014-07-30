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
package org.activiti.engine.impl.bpmn.parser.handler;

import java.util.Map;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.impl.bpmn.data.IOSpecification;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.process.ActivityImpl;


/**
 * @author Joram Barrez
 */
public class SubProcessParseHandler extends AbstractActivityBpmnParseHandler<SubProcess> {

  protected Class< ? extends BaseElement> getHandledType() {
    return SubProcess.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, SubProcess subProcess) {
    
    ActivityImpl activity = createActivityOnScope(bpmnParse, subProcess, BpmnXMLConstants.ELEMENT_SUBPROCESS, bpmnParse.getCurrentScope());
    
    activity.setAsync(subProcess.isAsynchronous());
    activity.setExclusive(!subProcess.isNotExclusive());

    boolean triggeredByEvent = false;
    if (subProcess instanceof EventSubProcess) {
      triggeredByEvent = true;
    }
    activity.setProperty("triggeredByEvent", triggeredByEvent);
    
    // event subprocesses are not scopes
    activity.setScope(!triggeredByEvent);
    activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createSubprocActivityBehavior(subProcess));
    
    bpmnParse.setCurrentScope(activity);
    bpmnParse.setCurrentSubProcess(subProcess);
    
    bpmnParse.processFlowElements(subProcess.getFlowElements());
    processArtifacts(bpmnParse, subProcess.getArtifacts(), activity);
    
    // no data objects for event subprocesses
    if (!(subProcess instanceof EventSubProcess)) {
      // parse out any data objects from the template in order to set up the necessary process variables
      Map<String, Object> variables = processDataObjects(bpmnParse, subProcess.getDataObjects(), activity);
      activity.setVariables(variables);
    }

    bpmnParse.removeCurrentScope();
    bpmnParse.removeCurrentSubProcess();
    
    if (subProcess.getIoSpecification() != null) {
      IOSpecification ioSpecification = createIOSpecification(bpmnParse, subProcess.getIoSpecification());
      activity.setIoSpecification(ioSpecification);
    }

  }

}
