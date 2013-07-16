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
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.behavior.EventBasedGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Joram Barrez
 */
public class SequenceFlowParseHandler extends AbstractBpmnParseHandler<SequenceFlow> {
  
  public static final String PROPERTYNAME_CONDITION = "condition";
  public static final String PROPERTYNAME_CONDITION_TEXT = "conditionText";

  public Class< ? extends BaseElement> getHandledType() {
    return SequenceFlow.class;
  }

  protected void executeParse(BpmnParse bpmnParse, SequenceFlow sequenceFlow) {
    
    BpmnModel bpmnModel = bpmnParse.getBpmnModel();
    ScopeImpl scope = bpmnParse.getCurrentScope();

    // Implicit check: sequence flow cannot cross (sub) process boundaries: we
    // don't do a processDefinition.findActivity here
    ActivityImpl sourceActivity = scope.findActivity(sequenceFlow.getSourceRef());
    ActivityImpl destinationActivity = scope.findActivity(sequenceFlow.getTargetRef());

    if (sourceActivity == null) {
      bpmnModel.addProblem("Invalid source '" + sequenceFlow.getSourceRef() + "' of sequence flow '" + sequenceFlow.getId() + "'", sequenceFlow);
    } else if (destinationActivity == null) {
      throw new ActivitiException("Invalid destination '" + sequenceFlow.getTargetRef() + "' of sequence flow '" + sequenceFlow.getId() + "'");
    } else if (!(sourceActivity.getActivityBehavior() instanceof EventBasedGatewayActivityBehavior)
            && destinationActivity.getActivityBehavior() instanceof IntermediateCatchEventActivityBehavior && (destinationActivity.getParentActivity() != null)
            && (destinationActivity.getParentActivity().getActivityBehavior() instanceof EventBasedGatewayActivityBehavior)) {

      bpmnModel.addProblem("Invalid incoming sequenceflow " + sequenceFlow.getId() + " for intermediateCatchEvent with id '" + destinationActivity.getId()
              + "' connected to an event-based gateway.", sequenceFlow);
    } else {

      TransitionImpl transition = sourceActivity.createOutgoingTransition(sequenceFlow.getId());
      bpmnParse.getSequenceFlows().put(sequenceFlow.getId(), transition);
      transition.setProperty("name", sequenceFlow.getName());
      transition.setProperty("documentation", sequenceFlow.getDocumentation());
      transition.setDestination(destinationActivity);

      if (StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())) {
        Condition expressionCondition = new UelExpressionCondition(bpmnParse.getExpressionManager().createExpression(sequenceFlow.getConditionExpression()));
        transition.setProperty(PROPERTYNAME_CONDITION_TEXT, sequenceFlow.getConditionExpression());
        transition.setProperty(PROPERTYNAME_CONDITION, expressionCondition);
      }

      createExecutionListenersOnTransition(bpmnParse, sequenceFlow.getExecutionListeners(), transition);
    }

  }

}
