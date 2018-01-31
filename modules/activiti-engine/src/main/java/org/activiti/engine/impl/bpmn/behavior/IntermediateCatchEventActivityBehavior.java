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


public class IntermediateCatchEventActivityBehavior extends AbstractBpmnActivityBehavior {

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    
    // Find active subscriptions
    List<EventSubscriptionEntity> subscriptions = ((ExecutionEntity)execution).getEventSubscriptions();
    
    if(!subscriptions.isEmpty()) {
      // There can be only one subscription entity 
      EventSubscriptionEntity subscription = subscriptions.get(0);
      
      if(isSignalEventAlreadyFired(execution, subscription)) {
        // Handle signal event for matching throw signal 
        subscription.eventReceived(null, false);
      }
    }
    
    // Do nothing: waitstate behavior
  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }
}

