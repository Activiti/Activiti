/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.bpmn.helper;

import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * An {@link ActivitiEventListener} that throws a signal event when an event is dispatched to it.
 *

 *
 */
public class SignalThrowingEventListener extends BaseDelegateEventListener {

  protected String signalName;
  protected boolean processInstanceScope = true;

  @Override
  public void onEvent(ActivitiEvent event) {
    if (isValidEvent(event)) {

      if (event.getProcessInstanceId() == null && processInstanceScope) {
        throw new ActivitiIllegalArgumentException("Cannot throw process-instance scoped signal, since the dispatched event is not part of an ongoing process instance");
      }

      CommandContext commandContext = Context.getCommandContext();
      EventSubscriptionEntityManager eventSubscriptionEntityManager = commandContext.getEventSubscriptionEntityManager();
      List<SignalEventSubscriptionEntity> subscriptionEntities = null;
      if (processInstanceScope) {
        subscriptionEntities = eventSubscriptionEntityManager.findSignalEventSubscriptionsByProcessInstanceAndEventName(event.getProcessInstanceId(), signalName);
      } else {
        String tenantId = null;
        if (event.getProcessDefinitionId() != null) {
          ProcessDefinition processDefinition = commandContext.getProcessEngineConfiguration().getDeploymentManager().findDeployedProcessDefinitionById(event.getProcessDefinitionId());
          tenantId = processDefinition.getTenantId();
        }
        subscriptionEntities = eventSubscriptionEntityManager.findSignalEventSubscriptionsByEventName(signalName, tenantId);
      }

      for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : subscriptionEntities) {
        eventSubscriptionEntityManager.eventReceived(signalEventSubscriptionEntity, null, false);
      }
    }
  }

  public void setSignalName(String signalName) {
    this.signalName = signalName;
  }

  public void setProcessInstanceScope(boolean processInstanceScope) {
    this.processInstanceScope = processInstanceScope;
  }

  @Override
  public boolean isFailOnException() {
    return true;
  }
}
