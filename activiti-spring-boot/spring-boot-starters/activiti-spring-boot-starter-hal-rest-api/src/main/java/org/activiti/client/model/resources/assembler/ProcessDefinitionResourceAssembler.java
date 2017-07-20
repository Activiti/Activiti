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

package org.activiti.client.model.resources.assembler;

import org.activiti.client.model.ProcessDefinition;
import org.activiti.client.model.resources.ProcessDefinitionResource;
import org.activiti.controllers.HomeController;
import org.activiti.controllers.ProcessDefinitionController;
import org.activiti.controllers.ProcessDefinitionMetaController;
import org.activiti.controllers.ProcessInstanceController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ProcessDefinitionResourceAssembler extends ResourceAssemblerSupport<ProcessDefinition, ProcessDefinitionResource> {

    public ProcessDefinitionResourceAssembler() {
        super(ProcessDefinitionController.class,
              ProcessDefinitionResource.class);
    }

    @Override
    public ProcessDefinitionResource toResource(ProcessDefinition processDefinition) {
    	
    	Link metadata = linkTo(methodOn(ProcessDefinitionMetaController.class).getProcessDefinitionMetadata(processDefinition.getId())).withRel("meta");
        Link selfRel = linkTo(methodOn(ProcessDefinitionController.class).getProcessDefinition(processDefinition.getId())).withSelfRel();
        Link startProcessLink = linkTo(methodOn(ProcessInstanceController.class).startProcess(null)).withRel("startProcess");
        Link homeLink = linkTo(HomeController.class).withRel("home");
        
        return new ProcessDefinitionResource(processDefinition,
        									 metadata,
                                             selfRel,
                                             startProcessLink,
                                             homeLink);
    }
}
