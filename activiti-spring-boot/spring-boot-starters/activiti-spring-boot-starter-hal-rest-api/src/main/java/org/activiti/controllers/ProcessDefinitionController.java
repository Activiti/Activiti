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

package org.activiti.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.client.model.ProcessDefinition;
import org.activiti.client.model.resources.ProcessDefinitionResource;
import org.activiti.client.model.resources.assembler.ProcessDefinitionResourceAssembler;
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
        return pagedResourcesAssembler.toResource(page, resourceAssembler);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ProcessDefinitionResource getProcessDefinition(@PathVariable String id) {
        org.activiti.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        if (processDefinition == null) {
            throw new ActivitiException("Unable to find process definition for the given id:'" + id + "'");
        }
        return resourceAssembler.toResource(processDefinitionConverter.from(processDefinition));
    }
    
    @RequestMapping(value = "/{id}/meta", method = RequestMethod.GET)
    public ProcessDefinitionResource getProcessDefinitionMetadata(@PathVariable String id) {
    	org.activiti.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        if (processDefinition == null) {
            throw new ActivitiException("Unable to find process definition for the given id:'" + id + "'");
        }
        Process process = repositoryService.getBpmnModel(id).getMainProcess();
        List<String []> variables = new ArrayList<String[]>();
        if(!process.getExtensionElements().isEmpty())
        {
        	Iterator<List<ExtensionElement>> it = process.getExtensionElements().values().iterator();
        	while(it.hasNext())
        	{
        		List<ExtensionElement> extensionElementList = it.next();
        		Iterator<ExtensionElement> it2 = extensionElementList.iterator();
        		while(it2.hasNext())
        		{
        			ExtensionElement ee = it2.next();
        			String[] variable = new String[2];
        			variable[0] = ee.getAttributeValue(ee.getNamespace(), "variableName");
        			variable[1] = ee.getAttributeValue(ee.getNamespace(), "variableType");
        			variables.add(variable);
        		}
        	}
        }
        List<String> users = new ArrayList<String>();
        List<String> groups = new ArrayList<String>();
        List<String []> userTasks = new ArrayList<String []>();
        List<String []> serviceTasks = new ArrayList<String []>();
        List<FlowElement> flowElementList = (List<FlowElement>) process.getFlowElements();
        for (FlowElement f : flowElementList) {
        	if(f.getClass().equals(UserTask.class))
        	{
        		UserTask userTask = (UserTask) f;
        		String[] t = new String[2];
        		t[0] = userTask.getName();
        		t[1] = userTask.getDocumentation();
        		userTasks.add(t);
        		users.addAll(userTask.getCandidateUsers());
        		groups.addAll(userTask.getCandidateGroups());
        	}
        	if(f.getClass().equals(ServiceTask.class))
        	{
        		ServiceTask serviceTask = (ServiceTask) f;
        		String[] s = new String[2];
        		s[0] = serviceTask.getName();
        		s[1] = serviceTask.getImplementation();
        		serviceTasks.add(s);
        	}
        }
        
        ProcessDefinition pd = new ProcessDefinition(
        		processDefinition.getId(), 
        		processDefinition.getName(), 
        		processDefinition.getDescription(), 
        		processDefinition.getVersion(), 
        		users, 
        		groups, 
        		variables, 
        		userTasks, 
        		serviceTasks);
        
        return resourceAssembler.toResource(pd);
    }
}
