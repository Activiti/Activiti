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

import org.activiti.services.core.model.ProcessInstanceVariables;
import org.activiti.services.rest.resources.assembler.ProcessVariableResourceAssembler;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**

 */
@RestController
@RequestMapping(value = "process-instances/{processInstanceId}/variables", produces = MediaTypes.HAL_JSON_VALUE)
public class ProcessInstanceVariableController {

    private final RuntimeService runtimeService;
    private final ProcessVariableResourceAssembler variableResourceBuilder;

    @Autowired
    public ProcessInstanceVariableController(RuntimeService runtimeService, ProcessVariableResourceAssembler variableResourceBuilder) {
        this.runtimeService = runtimeService;
        this.variableResourceBuilder = variableResourceBuilder;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Resource<Map<String, Object>> getVariables(@PathVariable String processInstanceId) {
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);

        return variableResourceBuilder.toResource(new ProcessInstanceVariables(processInstanceId, variables));
    }

}
