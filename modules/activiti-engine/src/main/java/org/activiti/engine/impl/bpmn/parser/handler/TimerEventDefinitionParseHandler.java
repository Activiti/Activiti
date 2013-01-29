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

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;


/**
 * @author Joram Barrez
 */
public class TimerEventDefinitionParseHandler extends AbstractMultiInstanceEnabledParseHandler<TimerEventDefinition> {
  
  public static final String PROPERTYNAME_START_TIMER = "timerStart";
  
  public Class< ? extends BaseElement> getHandledType() {
    return TimerEventDefinition.class;
  }
  
  protected void executeParse(BpmnParse bpmnParse, TimerEventDefinition timerEventDefinition, ScopeImpl scope, ActivityImpl timerActivity, SubProcess subProcess) {
    
    if (bpmnParse.getCurrentFlowElement() instanceof StartEvent) {
      
      ProcessDefinitionEntity processDefinition = bpmnParse.getCurrentProcessDefinition();
      timerActivity.setProperty("type", "startTimerEvent");
      TimerDeclarationImpl timerDeclaration = createTimer(bpmnParse, timerEventDefinition, timerActivity, TimerStartEventJobHandler.TYPE);
      timerDeclaration.setJobHandlerConfiguration(processDefinition.getKey());    
  
      List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(PROPERTYNAME_START_TIMER);
      if (timerDeclarations == null) {
        timerDeclarations = new ArrayList<TimerDeclarationImpl>();
        processDefinition.setProperty(PROPERTYNAME_START_TIMER, timerDeclarations);
      }
      timerDeclarations.add(timerDeclaration);
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof IntermediateCatchEvent) {
      
      timerActivity.setProperty("type", "intermediateTimer");
      TimerDeclarationImpl timerDeclaration = createTimer(bpmnParse, timerEventDefinition, timerActivity, TimerCatchIntermediateEventJobHandler.TYPE);
      if (getPrecedingEventBasedGateway(bpmnParse, (IntermediateCatchEvent) bpmnParse.getCurrentFlowElement()) != null) {
        addTimerDeclaration(timerActivity.getParent(), timerDeclaration);
      } else {
        addTimerDeclaration(timerActivity, timerDeclaration);
        timerActivity.setScope(true);
      }
      
    } else if (bpmnParse.getCurrentFlowElement() instanceof BoundaryEvent) {
      
      timerActivity.setProperty("type", "boundaryTimer");
      TimerDeclarationImpl timerDeclaration = createTimer(bpmnParse, timerEventDefinition, timerActivity, TimerExecuteNestedActivityJobHandler.TYPE);
      
      // ACT-1427
      BoundaryEvent boundaryEvent = (BoundaryEvent) bpmnParse.getCurrentFlowElement();
      boolean interrupting = boundaryEvent.isCancelActivity();
      if (interrupting) {
        timerDeclaration.setInterruptingTimer(true);
      }
      
      addTimerDeclaration(timerActivity.getParent(), timerDeclaration);

      if (timerActivity.getParent() instanceof ActivityImpl) {
        ((ActivityImpl) timerActivity.getParent()).setScope(true);
      }

      timerActivity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory()
              .createBoundaryEventActivityBehavior((BoundaryEvent) bpmnParse.getCurrentFlowElement(), interrupting, timerActivity));
      
    }
  }

}
