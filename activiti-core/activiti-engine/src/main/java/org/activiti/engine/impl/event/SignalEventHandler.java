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

package org.activiti.engine.impl.event;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.ActivitiSignalEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.impl.util.ProcessInstanceHelper;
import org.activiti.engine.repository.ProcessDefinition;

import java.util.Map;

/**


 */
public class SignalEventHandler extends AbstractEventHandler {

  public static final String EVENT_HANDLER_TYPE = "signal";

  public String getEventHandlerType() {
    return EVENT_HANDLER_TYPE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, CommandContext commandContext) {
    if (eventSubscription.getExecutionId() != null) {

      dispatchActivitySignalledEvent(eventSubscription.getExecution(),
                                     eventSubscription.getEventName(),
                                     payload,
                                     commandContext);

      super.handleEvent(eventSubscription, payload, commandContext);

    } else if (eventSubscription.getProcessDefinitionId() != null) {

      // Find initial flow element matching the signal start event
      String processDefinitionId = eventSubscription.getProcessDefinitionId();
      ProcessDefinition processDefinition = ProcessDefinitionUtil.getProcessDefinition(processDefinitionId);

      if (processDefinition == null) {
        throw new ActivitiObjectNotFoundException("No process definition found for id '" + processDefinitionId + "'", ProcessDefinition.class);
      }

      if (processDefinition.isSuspended()) {
        throw new ActivitiException("Could not handle signal: process definition with id: " + processDefinitionId + " is suspended");
      }

      org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
      FlowElement flowElement = process.getFlowElement(eventSubscription.getActivityId(), true);
      if (flowElement == null) {
        throw new ActivitiException("Could not find matching FlowElement for activityId " + eventSubscription.getActivityId());
      }

      // Start process instance via that flow element
      Map<String, Object> variables = null;
      if (payload instanceof Map) {
        variables = (Map<String, Object>) payload;
      }

      ProcessInstanceHelper processInstanceHelper = commandContext.getProcessEngineConfiguration()
                                                                  .getProcessInstanceHelper();

      ExecutionEntity executionEntity = processInstanceHelper.createProcessInstanceWithInitialFlowElement(processDefinition,
                                                                                                          null,
                                                                                                          null,
                                                                                                          flowElement,
                                                                                                          process);
      DelegateExecution execution = executionEntity.getExecutions()
                                                   .get(0);
      dispatchActivitySignalledEvent(execution,
                                     eventSubscription.getEventName(),
                                     payload,
                                     commandContext);

      processInstanceHelper.startProcessInstance(executionEntity,
                                                 commandContext,
                                                 variables,
                                                 flowElement,
                                 null);
    } else {
      throw new ActivitiException("Invalid signal handling: no execution nor process definition set");
    }
  }

  protected void dispatchActivitySignalledEvent(DelegateExecution execution,
                                     String signalName,
                                     Object payload,
                                     CommandContext commandContext) {
    if (commandContext.getProcessEngineConfiguration()
                      .getEventDispatcher()
                      .isEnabled()) {

      ActivitiSignalEvent signalEvent = ActivitiEventBuilder.createActivitiySignalledEvent(execution,
                                                                                           signalName,
                                                                                           payload);

      Context.getProcessEngineConfiguration().getEventDispatcher()
                                             .dispatchEvent(signalEvent);
    }
  }

}
