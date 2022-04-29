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

package org.activiti.engine.delegate.event.impl;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * Class capable of dispatching events.
 *

 */
public class ActivitiEventDispatcherImpl implements ActivitiEventDispatcher {

  protected ActivitiEventSupport eventSupport;
  protected boolean enabled = true;

  public ActivitiEventDispatcherImpl() {
    eventSupport = new ActivitiEventSupport();
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void addEventListener(ActivitiEventListener listenerToAdd) {
    eventSupport.addEventListener(listenerToAdd);
  }

  @Override
  public void addEventListener(ActivitiEventListener listenerToAdd, ActivitiEventType... types) {
    eventSupport.addEventListener(listenerToAdd, types);
  }

  @Override
  public void removeEventListener(ActivitiEventListener listenerToRemove) {
    eventSupport.removeEventListener(listenerToRemove);
  }

  @Override
  public void dispatchEvent(ActivitiEvent event) {
    if (enabled) {
      eventSupport.dispatchEvent(event);
    }

    if (event.getType() == ActivitiEventType.ENTITY_DELETED && event instanceof ActivitiEntityEvent) {
      ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
      if (entityEvent.getEntity() instanceof ProcessDefinition) {
        // process definition deleted event doesn't need to be dispatched to event listeners
        return;
      }
    }

    // Try getting hold of the Process definition, based on the process definition key, if a context is active
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext != null) {
      BpmnModel bpmnModel = extractBpmnModelFromEvent(event);
      if (bpmnModel != null) {
        ((ActivitiEventSupport) bpmnModel.getEventSupport()).dispatchEvent(event);
      }
    }

  }

  /**
   * In case no process-context is active, this method attempts to extract a process-definition based on the event. In case it's an event related to an entity, this can be deducted by inspecting the
   * entity, without additional queries to the database.
   *
   * If not an entity-related event, the process-definition will be retrieved based on the processDefinitionId (if filled in). This requires an additional query to the database in case not already
   * cached. However, queries will only occur when the definition is not yet in the cache, which is very unlikely to happen, unless evicted.
   *
   * @param event
   * @return
   */
  protected BpmnModel extractBpmnModelFromEvent(ActivitiEvent event) {
    BpmnModel result = null;

    if (result == null && event.getProcessDefinitionId() != null) {
      ProcessDefinition processDefinition = ProcessDefinitionUtil.getProcessDefinition(event.getProcessDefinitionId(), true);
      if (processDefinition != null) {
        result = Context.getProcessEngineConfiguration().getDeploymentManager().resolveProcessDefinition(processDefinition).getBpmnModel();
      }
    }

    return result;
  }

}
