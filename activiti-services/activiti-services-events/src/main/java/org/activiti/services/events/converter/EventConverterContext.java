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

package org.activiti.services.events.converter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventConverterContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConverterContext.class);

    private Map<ActivitiEventType, EventConverter> convertersMap;

    public EventConverterContext(Map<ActivitiEventType, EventConverter> convertersMap) {
        this.convertersMap = convertersMap;
    }

    @Autowired
    public EventConverterContext(Set<EventConverter> converters) {
        this.convertersMap = converters.stream().collect(Collectors.toMap(EventConverter::handledType,
                                                                          Function.identity()));
    }

    Map<ActivitiEventType, EventConverter> getConvertersMap() {
        return Collections.unmodifiableMap(convertersMap);
    }

    public ProcessEngineEvent from(ActivitiEvent activitiEvent) {
        EventConverter converter = convertersMap.get(activitiEvent.getType());
        ProcessEngineEvent newEvent = null;
        if (converter != null) {
            newEvent = converter.from(activitiEvent);
        } else {
            LOGGER.debug(">> Ommited Event Type: " + activitiEvent.getClass().getCanonicalName());
        }
        return newEvent;
    }
}
