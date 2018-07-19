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

package org.activiti.runtime.api.event.impl;

import org.activiti.runtime.api.event.CloudProcessSuspended;
import org.activiti.runtime.api.event.ProcessRuntimeEvent;
import org.activiti.runtime.api.model.ProcessInstance;

public class CloudProcessSuspendedEventImpl extends CloudRuntimeEventImpl<ProcessInstance, ProcessRuntimeEvent.ProcessEvents> implements CloudProcessSuspended {

    public CloudProcessSuspendedEventImpl() {
    }

    public CloudProcessSuspendedEventImpl(ProcessInstance processInstance) {
        super(processInstance);
        if(processInstance != null) {
            setEntityId(processInstance.getId());
        }
    }

    public CloudProcessSuspendedEventImpl(String id,
                                          Long timestamp,
                                          ProcessInstance processInstance) {
        super(id,
              timestamp,
              processInstance);
        if(processInstance != null) {
            setEntityId(processInstance.getId());
        }
    }

    @Override
    public ProcessEvents getEventType() {
        return ProcessEvents.PROCESS_SUSPENDED;
    }
}
