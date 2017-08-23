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

package org.activiti.services.rest.api.resources.assembler;

import org.activiti.services.core.model.ProcessInstanceVariables;
import org.activiti.services.rest.api.HomeController;
import org.activiti.services.rest.api.ProcessInstanceController;
import org.activiti.services.rest.api.ProcessInstanceVariableController;
import org.activiti.services.rest.api.resources.VariablesResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ProcessVariableResourceAssembler extends ResourceAssemblerSupport<ProcessInstanceVariables, VariablesResource> {

    public ProcessVariableResourceAssembler() {
        super(ProcessInstanceVariableController.class,
              VariablesResource.class);
    }

    @Override
    public VariablesResource toResource(ProcessInstanceVariables processInstanceVariables) {
        Link selfRel = linkTo(methodOn(ProcessInstanceVariableController.class).getVariables(processInstanceVariables.getProcessInstanceId())).withSelfRel();
        Link processInstanceRel = linkTo(methodOn(ProcessInstanceController.class).getProcessInstanceById(processInstanceVariables.getProcessInstanceId())).withRel("processInstance");
        Link homeLink = linkTo(HomeController.class).withRel("home");
        return new VariablesResource(processInstanceVariables.getVariables(),
                                     selfRel,
                                     processInstanceRel,
                                     homeLink);
    }
}
