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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.services.core.model.ProcessDefinition;
import org.activiti.services.core.model.converter.ProcessDefinitionConverter;
import org.activiti.services.core.pageable.PageableRepositoryService;
import org.activiti.services.rest.api.ProcessDefinitionController;
import org.activiti.services.rest.api.resources.ProcessDefinitionResource;
import org.activiti.services.rest.api.resources.assembler.ProcessDefinitionResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessDefinitionControllerImpl implements ProcessDefinitionController {

    private final RepositoryService repositoryService;

    private final ProcessDiagramGenerator processDiagramGenerator;

    private final ProcessDefinitionConverter processDefinitionConverter;

    private final ProcessDefinitionResourceAssembler resourceAssembler;

    private final PageableRepositoryService pageableRepositoryService;

    @Autowired
    public ProcessDefinitionControllerImpl(RepositoryService repositoryService,
                                           ProcessDiagramGenerator processDiagramGenerator,
                                           ProcessDefinitionConverter processDefinitionConverter,
                                           ProcessDefinitionResourceAssembler resourceAssembler,
                                           PageableRepositoryService pageableRepositoryService) {
        this.repositoryService = repositoryService;
        this.processDiagramGenerator = processDiagramGenerator;
        this.processDefinitionConverter = processDefinitionConverter;
        this.resourceAssembler = resourceAssembler;
        this.pageableRepositoryService = pageableRepositoryService;
    }

    @Override
    public PagedResources<ProcessDefinitionResource> getProcessDefinitions(Pageable pageable,
                                                                           PagedResourcesAssembler<ProcessDefinition> pagedResourcesAssembler) {
        Page<ProcessDefinition> page = pageableRepositoryService.getProcessDefinitions(pageable);
        return pagedResourcesAssembler.toResource(page,
                                                  resourceAssembler);
    }

    @Override
    public ProcessDefinitionResource getProcessDefinition(@PathVariable String id) {
        org.activiti.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id)
                .singleResult();
        if (processDefinition == null) {
            throw new ActivitiException("Unable to find process definition for the given id:'" + id + "'");
        }
        return resourceAssembler.toResource(processDefinitionConverter.from(processDefinition));
    }

    @Override
    public String getProcessModel(@PathVariable String id) {
        try (final InputStream resourceStream = repositoryService.getProcessModel(id)) {
            return new String(IoUtil.readInputStream(resourceStream,
                                                     null),
                              StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ActivitiException("Error occured while getting process model '" + id + "' : " + e.getMessage(),
                                        e);
        }
    }

    @Override
    public String getBpmnModel(@PathVariable String id) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(id);
        ObjectNode json = new BpmnJsonConverter().convertToJson(bpmnModel);
        return json.toString();
    }

    @Override
    public String getProcessDiagram(@PathVariable  String id) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(id);
        String activityFontName = processDiagramGenerator.getDefaultActivityFontName();
        String labelFontName = processDiagramGenerator.getDefaultLabelFontName();
        String annotationFontName = processDiagramGenerator.getDefaultAnnotationFontName();
        try (final InputStream imageStream = processDiagramGenerator.generateDiagram(bpmnModel,
                                                                                     activityFontName,
                                                                                     labelFontName,
                                                                                     annotationFontName)) {
            return new String(IoUtil.readInputStream(imageStream,
                                                     null),
                              StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ActivitiException("Error occured while getting process diagram '" + id + "' : " + e.getMessage(),
                                        e);
        }
    }
}
