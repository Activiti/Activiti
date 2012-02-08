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

import org.activiti.engine.impl.bpmn.parser.SignalEventDefinition;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


/**
 * @author Daniel Meyer
 */
public class IntermediateThrowSignalEventActivityBehavior extends AbstractBpmnActivityBehavior {    
      
  protected final SignalEventDefinition signalDefinition;

  public IntermediateThrowSignalEventActivityBehavior(SignalEventDefinition signalDefinition) {
    this.signalDefinition = signalDefinition;
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    
    CommandContext commandContext = Context.getCommandContext();
    
    List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName = commandContext
      .getEventSubscriptionManager()
      .findSignalEventSubscriptionsByEventName(signalDefinition.getSignalName());
    
    for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : findSignalEventSubscriptionsByEventName) {
      signalEventSubscriptionEntity.eventReceived(null, signalDefinition.isAsync());
    }
    
    leave(execution);        
  }
 
}
