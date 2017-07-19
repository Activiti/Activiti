/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.services.rest.controllers;

import org.activiti.services.rest.resources.ProcessDefinitionResource;
import org.activiti.services.rest.resources.assembler.ProcessDefinitionResourceAssembler;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.model.converter.ProcessDefinitionConverter;
import org.activiti.services.PageableRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "process-definitions", produces = MediaTypes.HAL_JSON_VALUE)
public class ProcessDefinitionController {

    private final RepositoryService repositoryService;

    private final ProcessDefinitionConverter processDefinitionConverter;

    private final ProcessDefinitionResourceAssembler resourceAssembler;

    private final PageableRepositoryService pageableRepositoryService;

    @Autowired
    public ProcessDefinitionController(RepositoryService repositoryService,
                                       ProcessDefinitionConverter processDefinitionConverter,
                                       ProcessDefinitionResourceAssembler resourceAssembler,
                                       PageableRepositoryService pageableRepositoryService) {
        this.repositoryService = repositoryService;
        this.processDefinitionConverter = processDefinitionConverter;
        this.resourceAssembler = resourceAssembler;
        this.pageableRepositoryService = pageableRepositoryService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<ProcessDefinitionResource> getProcessDefinitions(Pageable pageable,
                                                                           PagedResourcesAssembler<ProcessDefinition> pagedResourcesAssembler) {
        Page<ProcessDefinition> page = pageableRepositoryService.getProcessDefinitions(pageable);
        return pagedResourcesAssembler.toResource(page,
                                                  resourceAssembler);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ProcessDefinitionResource getProcessDefinition(@PathVariable String id) {
        org.activiti.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        if (processDefinition == null) {
            throw new ActivitiException("Unable to find process definition for the given id:'" + id + "'");
        }
        return resourceAssembler.toResource(processDefinitionConverter.from(processDefinition));
    }
}
