/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.model.impl;

import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.runtime.model.impl.BPMNActivityImpl;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;

public class ToActivityConverter {

    public BPMNActivity from(ActivitiActivityEvent internalEvent) {
        BPMNActivityImpl activity = new BPMNActivityImpl(internalEvent.getActivityId(),
                                                             internalEvent.getActivityName(),
                                                             internalEvent.getActivityType());
        activity.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        activity.setProcessInstanceId(internalEvent.getProcessInstanceId());
        activity.setExecutionId(internalEvent.getExecutionId());

        return activity;
    }

}
