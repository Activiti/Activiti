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

import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


/**
 * @author Daniel Meyer
 */
public class EventBasedGatewayActivityBehavior extends FlowNodeActivityBehavior {

  
  @Override
  public void execute(ActivityExecution execution) throws Exception {
      
    List<EventSubscriptionEntity> subscriptions = ((ExecutionEntity)execution).getEventSubscriptions();

    // Continue with signal catch event activities for signal events
    // already fired and registered in process instance execution scope
    for(EventSubscriptionEntity subscription: subscriptions) {
      if(isSignalEventAlreadyFired(execution, subscription))
      {
          // Execute signal event behavior 
          subscription.eventReceived(null, false);

          // Interrupt loop to handle only one out of many signals after the gateway
          break;
      }
    }
    
    // Otherwise
    // the event based gateway doesn't really do anything
    // ignoring outgoing sequence flows (they're only parsed for the diagram)
  }
  
}
