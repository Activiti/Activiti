/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.api.runtime.event.impl;

import java.util.List;

import org.activiti.api.process.model.events.StartMessageDeployedEvent;
import org.springframework.context.ApplicationEvent;

public class StartMessageDeployedEvents extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    public StartMessageDeployedEvents(List<StartMessageDeployedEvent> processDeployedEvents) {
        super(processDeployedEvents);
    }
    
    
    @SuppressWarnings("unchecked")
    public final List<StartMessageDeployedEvent> getStartMessageDeployedEvents() {
        return (List<StartMessageDeployedEvent>) getSource();
    }
    

}
