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

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.commands.SignalProcessInstanceCmd;
import org.activiti.services.core.model.commands.StartProcessInstanceCmd;
import org.activiti.services.rest.resources.ProcessInstanceResource;
import org.activiti.services.rest.resources.assembler.ProcessInstanceResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "process-instances", produces = MediaTypes.HAL_JSON_VALUE)
public class ProcessInstanceController {

    private ProcessEngineWrapper processEngine;

    private final ProcessInstanceResourceAssembler resourceAssembler;

    @Autowired
    public ProcessInstanceController(ProcessEngineWrapper processEngine,
                                     ProcessInstanceResourceAssembler resourceAssembler) {
        this.processEngine = processEngine;
        this.resourceAssembler = resourceAssembler;
    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<ProcessInstanceResource> getProcessInstances(Pageable pageable,
                                                                       PagedResourcesAssembler<ProcessInstance> pagedResourcesAssembler) {
        return pagedResourcesAssembler.toResource(processEngine.getProcessInstances(pageable),
                                                  resourceAssembler);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Resource<ProcessInstance> startProcess(@RequestBody StartProcessInstanceCmd cmd) {
        return resourceAssembler.toResource(processEngine.startProcess(cmd));
    }

    @RequestMapping(value = "/{processInstanceId}", method = RequestMethod.GET)
    public Resource<ProcessInstance> getProcessInstanceById(@PathVariable String processInstanceId) {
        return resourceAssembler.toResource(processEngine.getProcessInstanceById(processInstanceId));
    }

    @RequestMapping(value = "/signal")
    public ResponseEntity<Void> sendSignal(@RequestBody
                                                   SignalProcessInstanceCmd cmd) {
        processEngine.signal(cmd);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "{processInstanceId}/suspend")
    public ResponseEntity<Void> suspend(@PathVariable String processInstanceId) {
        processEngine.suspend(processInstanceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "{processInstanceId}/activate")
    public ResponseEntity<Void> activate(@PathVariable String processInstanceId) {
        processEngine.activate(processInstanceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
