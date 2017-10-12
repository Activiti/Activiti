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

package org.activiti.services.query.events.handlers;

import java.util.Date;

import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.services.query.events.ProcessStartedEvent;
import org.activiti.services.query.model.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessStartedHandler implements QueryEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStartedHandler.class);

    private ProcessInstanceRepository processInstanceRepository;

    @Autowired
    public ProcessStartedHandler(ProcessInstanceRepository processInstanceRepository) {
        this.processInstanceRepository = processInstanceRepository;
    }

    @Override
    public void handle(ProcessEngineEvent event) {
        LOGGER.debug("Handling start of process Instance " + event.getProcessInstanceId());

        processInstanceRepository.save(
                new ProcessInstance(event.getProcessInstanceId(),
                                    event.getProcessDefinitionId(),
                                    "RUNNING",
                                    new Date(event.getTimestamp())));

    }

    @Override
    public Class<? extends ProcessEngineEvent> getHandledEventClass() {
        return ProcessStartedEvent.class;
    }

}
