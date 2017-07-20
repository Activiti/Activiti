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

package org.activiti.services.query.app.assembler;

import org.activiti.services.query.app.ProcessInstanceQueryController;
import org.activiti.services.query.app.model.ProcessInstance;
import org.activiti.services.query.app.resources.ProcessInstanceQueryResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessInstanceQueryResourceAssembler extends ResourceAssemblerSupport<ProcessInstance,ProcessInstanceQueryResource> {


    public ProcessInstanceQueryResourceAssembler() {
        super(ProcessInstanceQueryController.class, ProcessInstanceQueryResource.class);
    }

    @Override
    public ProcessInstanceQueryResource toResource(ProcessInstance processInstance) {
        List<Link> links = new ArrayList<>();
        return new ProcessInstanceQueryResource(processInstance,links);
    }
}
