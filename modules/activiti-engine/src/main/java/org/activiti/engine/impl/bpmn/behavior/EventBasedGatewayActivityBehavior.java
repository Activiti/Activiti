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

import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;


/**
 * @author Daniel Meyer
 */
public class EventBasedGatewayActivityBehavior extends FlowNodeActivityBehavior {
  
  @Override
  public void execute(ActivityExecution execution) throws Exception {

    // Continue with signal catch event activities for signal events 
    // already fired and registered in process instance execution scope
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    ActivityImpl executionActivity = executionEntity.getActivity();
    
    for(ActivityImpl mayBeCatchEventActivity: executionActivity.getActivities()) {
      if(mayBeCatchEventActivity.getActivityBehavior() instanceof IntermediateCatchEventActivityBehavior) {
        IntermediateCatchEventActivityBehavior catchEventActivityBehavior = (IntermediateCatchEventActivityBehavior) mayBeCatchEventActivity.getActivityBehavior();

        SignalEventDefinition signalEventDef = findSignalEventDefinition(catchEventActivityBehavior);
        
        // Find matching registered signal event
        if(signalEventDef != null) {
          if(Context.getCommandContext().getSignalEventSessionManager()
              .hasThrowSignalEventForExecution(execution, signalEventDef.getSignalRef())) 
          {
              // Switch execution context to signal catch event activity
              executionEntity.setActivity(mayBeCatchEventActivity); 
              
              // Emit signal
              catchEventActivityBehavior.signal(execution, signalEventDef.getSignalRef(), null);
              
              return;
          }
        }
      }
    }
    
    // Otherwise
    // the event based gateway doesn't really do anything
    // ignoring outgoing sequence flows (they're only parsed for the diagram)
  }
 
  protected SignalEventDefinition findSignalEventDefinition(IntermediateCatchEventActivityBehavior catchEventActivityBehavior) {
    SignalEventDefinition signalEventDefinition = null;
    IntermediateCatchEvent catchEvent = catchEventActivityBehavior.intermediateCatchEvent;
    
    if(catchEvent != null) {
      for(EventDefinition eventDef : catchEvent.getEventDefinitions()) {
        if(eventDef instanceof SignalEventDefinition) {
          signalEventDefinition = (SignalEventDefinition) eventDef;
        }
      }
    }
    
    return signalEventDefinition;
    
  }  
}
