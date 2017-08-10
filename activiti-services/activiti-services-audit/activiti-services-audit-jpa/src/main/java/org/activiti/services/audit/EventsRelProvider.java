/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.audit;

import org.activiti.services.audit.events.ProcessEngineEventEntity;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.hateoas.RelProvider;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EventsRelProvider implements RelProvider {

    public static final String COLLECTION_RESOURCE_REL = "events";

    private static final String ITEM_RESOURCE_REL = "event";

    @Override
    public String getItemResourceRelFor(Class<?> aClass) {
        return ITEM_RESOURCE_REL;
    }

    @Override
    public String getCollectionResourceRelFor(Class<?> aClass) {
        return COLLECTION_RESOURCE_REL;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return ProcessEngineEventEntity.class.isAssignableFrom(aClass);
    }
}
