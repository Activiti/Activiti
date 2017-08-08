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
import java.util.Collections;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.commands.ActivateProcessInstanceCmd;
import org.activiti.services.core.model.commands.SignalProcessInstancesCmd;
import org.activiti.services.core.model.commands.StartProcessInstanceCmd;
import org.activiti.services.core.model.commands.SuspendProcessInstanceCmd;
import org.activiti.services.rest.api.ProcessInstanceController;
import org.activiti.services.rest.api.resources.ProcessInstanceResource;
import org.activiti.services.rest.api.resources.assembler.ProcessInstanceResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessInstanceControllerImpl implements ProcessInstanceController {

    private ProcessEngineWrapper processEngine;

    private final RepositoryService repositoryService;

    private final ProcessDiagramGenerator processDiagramGenerator;

    private final ProcessInstanceResourceAssembler resourceAssembler;

    @Autowired
    public ProcessInstanceControllerImpl(ProcessEngineWrapper processEngine,
                                         RepositoryService repositoryService,
                                         ProcessDiagramGenerator processDiagramGenerator,
                                         ProcessInstanceResourceAssembler resourceAssembler) {
        this.processEngine = processEngine;
        this.repositoryService = repositoryService;
        this.processDiagramGenerator = processDiagramGenerator;
        this.resourceAssembler = resourceAssembler;
    }

    @Override
    public PagedResources<ProcessInstanceResource> getProcessInstances(Pageable pageable,
                                                                       PagedResourcesAssembler<ProcessInstance> pagedResourcesAssembler) {
        return pagedResourcesAssembler.toResource(processEngine.getProcessInstances(pageable),
                                                  resourceAssembler);
    }

    @Override
    public Resource<ProcessInstance> startProcess(@RequestBody StartProcessInstanceCmd cmd) {

        return resourceAssembler.toResource(processEngine.startProcess(cmd));
    }

    @Override
    public Resource<ProcessInstance> getProcessInstanceById(@PathVariable String processInstanceId) {
        return resourceAssembler.toResource(processEngine.getProcessInstanceById(processInstanceId));
    }

    @Override
    public String getProcessDiagram(@PathVariable String processInstanceId) {
        ProcessInstance processInstance = processEngine.getProcessInstanceById(processInstanceId);
        if (processInstance == null) {
            throw new ActivitiException("Unable to find process instance for the given id:'" + processInstanceId + "'");
        }
        List<String> activityIds = processEngine.getActiveActivityIds(processInstanceId);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        String activityFontName = processDiagramGenerator.getDefaultActivityFontName();
        String labelFontName = processDiagramGenerator.getDefaultLabelFontName();
        String annotationFontName = processDiagramGenerator.getDefaultAnnotationFontName();
        try (final InputStream imageStream = processDiagramGenerator.generateDiagram(bpmnModel,
                                                                                     activityIds,
                                                                                     Collections.emptyList(),
                                                                                     activityFontName,
                                                                                     labelFontName,
                                                                                     annotationFontName)) {
            return new String(IoUtil.readInputStream(imageStream,
                                                     null),
                              StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ActivitiException("Error occured while getting process diagram '" + processInstanceId + "' : " + e.getMessage(),
                                        e);
        }
    }

    @Override
    public ResponseEntity<Void> sendSignal(@RequestBody SignalProcessInstancesCmd cmd) {
        processEngine.signal(cmd);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> suspend(@PathVariable String processInstanceId) {
        processEngine.suspend(new SuspendProcessInstanceCmd(processInstanceId));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> activate(@PathVariable String processInstanceId) {
        processEngine.activate(new ActivateProcessInstanceCmd(processInstanceId));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
