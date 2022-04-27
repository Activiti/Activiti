/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.engine.impl.persistence.entity.data.impl.cachematcher;

import java.util.Map;

import org.activiti.engine.impl.persistence.CachedEntityMatcherAdapter;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.apache.commons.lang3.StringUtils;


public class SignalEventSubscriptionByEventNameMatcher extends CachedEntityMatcherAdapter<EventSubscriptionEntity> {

  @Override
  public boolean isRetained(EventSubscriptionEntity eventSubscriptionEntity, Object parameter) {

    Map<String, String> params = (Map<String, String>) parameter;
    String eventName = params.get("eventName");
    String tenantId = params.get("tenantId");

    return eventSubscriptionEntity.getEventType() != null && eventSubscriptionEntity.getEventType().equals(SignalEventSubscriptionEntity.EVENT_TYPE)
        && eventSubscriptionEntity.getEventName() != null && eventSubscriptionEntity.getEventName().equals(eventName)
        && (eventSubscriptionEntity.getExecutionId() == null || (eventSubscriptionEntity.getExecutionId() != null && eventSubscriptionEntity.getExecution() != null && eventSubscriptionEntity.getExecution().getSuspensionState() == SuspensionState.ACTIVE.getStateCode()) )
        && ( (params.containsKey("tenantId") && tenantId.equals(eventSubscriptionEntity.getTenantId())) || (!params.containsKey("tenantId") && StringUtils.isEmpty(eventSubscriptionEntity.getTenantId())) );
  }

}
