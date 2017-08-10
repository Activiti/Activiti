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

import java.util.Map;

import org.activiti.engine.TaskService;
import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.TaskVariables;
import org.activiti.services.core.model.TaskVariables.TaskVariableScope;
import org.activiti.services.core.model.commands.SetTaskVariablesCmd;
import org.activiti.services.rest.api.TaskVariableController;
import org.activiti.services.rest.api.resources.assembler.TaskVariableResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/tasks/{taskId}/variables",
        produces = MediaTypes.HAL_JSON_VALUE)
public class TaskVariableControllerImpl implements TaskVariableController {

    private ProcessEngineWrapper processEngine;

    private final TaskService taskService;

    private final TaskVariableResourceAssembler variableResourceBuilder;

    @Autowired
    public TaskVariableControllerImpl(ProcessEngineWrapper processEngine,
                                      TaskService taskService,
                                      TaskVariableResourceAssembler variableResourceBuilder) {
        this.processEngine = processEngine;
        this.taskService = taskService;
        this.variableResourceBuilder = variableResourceBuilder;
    }

    @Override
    @RequestMapping(value = "/",
            method = RequestMethod.GET)
    public Resource<Map<String, Object>> getVariables(@PathVariable String taskId) {
        Map<String, Object> variables = taskService.getVariables(taskId);
        return variableResourceBuilder.toResource(new TaskVariables(taskId, variables, TaskVariableScope.GLOBAL));
    }

    @Override
    @RequestMapping(value = "/local",
            method = RequestMethod.GET)
    public Resource<Map<String, Object>> getVariablesLocal(@PathVariable String taskId) {
        Map<String, Object> variables = taskService.getVariablesLocal(taskId);
        return variableResourceBuilder.toResource(new TaskVariables(taskId, variables, TaskVariableScope.LOCAL));
    }

    @Override
    @RequestMapping(value = "/",
            method = RequestMethod.POST)
    public ResponseEntity<Void> setVariables(@PathVariable String taskId,
                                             @RequestBody(required = true) SetTaskVariablesCmd setTaskVariablesCmd) {
        processEngine.setTaskVariables(setTaskVariablesCmd);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @RequestMapping(value = "/local",
            method = RequestMethod.POST)
    public ResponseEntity<Void> setVariablesLocal(@PathVariable String taskId,
                                                  @RequestBody(
                                                          required = true) SetTaskVariablesCmd setTaskVariablesCmd) {
        processEngine.setTaskVariablesLocal(setTaskVariablesCmd);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
