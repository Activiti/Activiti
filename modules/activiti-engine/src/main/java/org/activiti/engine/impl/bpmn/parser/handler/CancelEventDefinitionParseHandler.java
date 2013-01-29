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

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

/**
 * @author Joram Barrez
 */
public class CancelEventDefinitionParseHandler extends AbstractBpmnParseHandler<CancelEventDefinition> {

  public Class< ? extends BaseElement> getHandledType() {
    return CancelEventDefinition.class;
  }

  protected void executeParse(BpmnParse bpmnParse, CancelEventDefinition cancelEventDefinition) {

    BpmnModel bpmnModel = bpmnParse.getBpmnModel();

    if (bpmnParse.getCurrentFlowElement() instanceof BoundaryEvent) {
      
      ActivityImpl activity = bpmnParse.getCurrentActivity();
      activity.setProperty("type", "cancelBoundaryCatch");
      ActivityImpl parent = (ActivityImpl) activity.getParent();
      
      if (!parent.getProperty("type").equals("transaction")) {
        bpmnModel.addProblem("boundary event with cancelEventDefinition only supported on transaction subprocesses.", cancelEventDefinition);
      }

      for (ActivityImpl child : parent.getActivities()) {
        if (child.getProperty("type").equals("cancelBoundaryCatch") && child != activity) {
          bpmnModel.addProblem("multiple boundary events with cancelEventDefinition not supported on same transaction subprocess.", cancelEventDefinition);
        }
      }

      activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createCancelBoundaryEventActivityBehavior(cancelEventDefinition));

    }

  }

}
