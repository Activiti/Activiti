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

import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


/**
 * @author Daniel Meyer
 */
public class IntermediateThrowSignalEventActivityBehavior extends AbstractBpmnActivityBehavior {    
      
  private static final long serialVersionUID = -2961893934810190972L;
  
  protected final boolean processInstanceScope;
  protected final EventSubscriptionDeclaration signalDefinition;

  public IntermediateThrowSignalEventActivityBehavior(ThrowEvent throwEvent, Signal signal, EventSubscriptionDeclaration signalDefinition) {
    this.processInstanceScope = Signal.SCOPE_PROCESS_INSTANCE.equals(signal.getScope());
    this.signalDefinition = signalDefinition;
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    
    CommandContext commandContext = Context.getCommandContext();
    
    List<SignalEventSubscriptionEntity> subscriptionEntities = null;
    if (processInstanceScope) {
      subscriptionEntities = commandContext
              .getEventSubscriptionEntityManager()
              .findSignalEventSubscriptionsByProcessInstanceAndEventName(execution.getProcessInstanceId(), signalDefinition.getEventName());
    } else {
      subscriptionEntities = commandContext
              .getEventSubscriptionEntityManager()
              .findSignalEventSubscriptionsByEventName(signalDefinition.getEventName(), execution.getTenantId());
    }
    
    for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : subscriptionEntities) {
      signalEventSubscriptionEntity.eventReceived(null, signalDefinition.isAsync());
    }
    
    if (execution.getActivity() != null) { // dont continue if process has already finished
      leave(execution);
    }
  }
 
}
