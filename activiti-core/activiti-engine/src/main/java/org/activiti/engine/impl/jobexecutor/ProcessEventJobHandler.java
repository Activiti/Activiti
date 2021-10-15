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

package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;

/**


 */
public class ProcessEventJobHandler implements JobHandler {

    public static final String TYPE = "event";

    public String getType() {
        return TYPE;
    }

    public void execute(
        JobEntity job,
        String configuration,
        ExecutionEntity execution,
        CommandContext commandContext
    ) {
        EventSubscriptionEntityManager eventSubscriptionEntityManager = commandContext.getEventSubscriptionEntityManager();

        // lookup subscription:
        EventSubscriptionEntity eventSubscriptionEntity = eventSubscriptionEntityManager.findById(
            configuration
        );

        // if event subscription is null, ignore
        if (eventSubscriptionEntity != null) {
            eventSubscriptionEntityManager.eventReceived(
                eventSubscriptionEntity,
                null,
                false
            );
        }
    }
}
