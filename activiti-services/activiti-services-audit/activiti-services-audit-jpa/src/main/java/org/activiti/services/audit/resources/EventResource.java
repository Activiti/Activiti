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

package org.activiti.services.audit.resources;

import org.activiti.services.audit.events.ProcessEngineEventEntity;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

public class EventResource extends Resource<ProcessEngineEventEntity> {

    public EventResource(ProcessEngineEventEntity content,
                         Link... links) {
        super(content,
              links);
    }

}
