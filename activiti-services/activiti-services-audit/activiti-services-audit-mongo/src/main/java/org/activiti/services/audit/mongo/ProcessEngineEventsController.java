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

package org.activiti.services.audit.mongo;

import java.util.Optional;

import com.querydsl.core.types.Predicate;
import org.activiti.engine.ActivitiException;
import org.activiti.services.audit.mongo.assembler.EventResourceAssembler;
import org.activiti.services.audit.mongo.events.ProcessEngineEventDocument;
import org.activiti.services.audit.mongo.repository.EventsRepository;
import org.activiti.services.audit.mongo.resources.EventResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/events")
public class ProcessEngineEventsController {

    private final EventsRepository eventsRepository;

    private EventResourceAssembler eventResourceAssembler;

    private PagedResourcesAssembler<ProcessEngineEventDocument> pagedResourcesAssembler;

    @Autowired
    public ProcessEngineEventsController(EventsRepository eventsRepository,
                                         EventResourceAssembler eventResourceAssembler,
                                         PagedResourcesAssembler<ProcessEngineEventDocument> pagedResourcesAssembler) {
        this.eventsRepository = eventsRepository;
        this.eventResourceAssembler = eventResourceAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @RequestMapping(value = "/{eventId}", method = RequestMethod.GET)
    public EventResource findById(@PathVariable String eventId) {
        Optional<ProcessEngineEventDocument> findResult = eventsRepository.findById(eventId);
        if (!findResult.isPresent()) {
            throw new ActivitiException("Unable to find event for the given id:'" + eventId + "'");
        } else {
            return eventResourceAssembler.toResource(findResult.get());
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<EventResource> findAll(@QuerydslPredicate(
            root = ProcessEngineEventDocument.class) Predicate predicate, Pageable pageable) {
        if (predicate == null) {
            return pagedResourcesAssembler.toResource(eventsRepository.findAll(pageable), eventResourceAssembler);
        } else {
            return pagedResourcesAssembler.toResource(eventsRepository.findAll(predicate, pageable),
                                                      eventResourceAssembler);
        }
    }
}
