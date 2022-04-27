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

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.persistence.CachedEntityMatcherAdapter;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;


public class EventSubscriptionsByNameMatcher extends CachedEntityMatcherAdapter<EventSubscriptionEntity> {

  @Override
  @SuppressWarnings("unchecked")
  public boolean isRetained(EventSubscriptionEntity eventSubscriptionEntity, Object parameter) {

    Map<String, String> params = (Map<String, String>) parameter;
    String type = params.get("eventType");
    String eventName = params.get("eventName");
    String tenantId = params.get("tenantId");

    if (eventSubscriptionEntity.getEventType() != null && eventSubscriptionEntity.getEventType().equals(type)
        && eventSubscriptionEntity.getEventName() != null && eventSubscriptionEntity.getEventName().equals(eventName)) {
      if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
        return eventSubscriptionEntity.getTenantId() != null && eventSubscriptionEntity.getTenantId().equals(tenantId);
      } else {
        return ProcessEngineConfiguration.NO_TENANT_ID.equals(eventSubscriptionEntity.getTenantId()) || eventSubscriptionEntity.getTenantId() == null;
      }
    }
    return false;
  }

}
