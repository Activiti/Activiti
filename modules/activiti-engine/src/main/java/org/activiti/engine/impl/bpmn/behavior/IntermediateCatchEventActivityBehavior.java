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

import java.util.List;

import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


public class IntermediateCatchEventActivityBehavior extends AbstractBpmnActivityBehavior {

  protected final IntermediateCatchEvent intermediateCatchEvent;
  
  public IntermediateCatchEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent) {
    this.intermediateCatchEvent = intermediateCatchEvent;
  }  

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    
    // Get all associated event definitions     
    List<EventDefinition> events = intermediateCatchEvent.getEventDefinitions();

    // Find signal event definition
    for(EventDefinition event : events) {
      if(event instanceof SignalEventDefinition) {
        SignalEventDefinition signalEventDefinition = (SignalEventDefinition) event;
        
        // Check already published signal events registered in the transaction context   
        if(Context.getCommandContext().getSignalEventSessionManager()
            .hasThrowSignalEventForExecution(execution, signalEventDefinition.getSignalRef()))
        {
            // Leave execution if matching throw signal found 
           leave(execution);
        }
      }
    }
    
    // Do nothing: waitstate behavior
  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }
}

