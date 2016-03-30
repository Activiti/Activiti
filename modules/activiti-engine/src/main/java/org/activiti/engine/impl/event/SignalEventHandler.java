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

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.ProcessInstanceHelper;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
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
      super.handleEvent(eventSubscription, payload, commandContext);

    } else if (eventSubscription.getProcessDefinitionId() != null) {
      // Start event
      String processDefinitionId = eventSubscription.getProcessDefinitionId();
      DeploymentManager deploymentCache = Context.getProcessEngineConfiguration().getDeploymentManager();

      ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
      if (processDefinition == null) {
        throw new ActivitiObjectNotFoundException("No process definition found for id '" + processDefinitionId + "'", ProcessDefinition.class);
      }

      Map<String, Object> variables = null;
      if (payload != null && payload instanceof Map) {
        variables = (Map<String, Object>) payload;
      }

      ProcessInstanceHelper processInstanceHelper = commandContext.getProcessEngineConfiguration().getProcessInstanceHelper();
      processInstanceHelper.createAndStartProcessInstance(processDefinition, null, null, variables);
    } else {
      throw new ActivitiException("Invalid signal handling: no execution nor process definition set");
    }
  }

}
