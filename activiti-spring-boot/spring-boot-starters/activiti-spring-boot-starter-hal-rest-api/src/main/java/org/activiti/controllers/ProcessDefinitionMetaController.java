package org.activiti.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.UserTask;
import org.activiti.client.model.ProcessDefinitionMeta;
import org.activiti.client.model.ProcessDefinitionServiceTask;
import org.activiti.client.model.ProcessDefinitionUserTask;
import org.activiti.client.model.ProcessDefinitionVariable;
import org.activiti.client.model.resources.ProcessDefinitionMetaResource;
import org.activiti.client.model.resources.assembler.ProcessDefinitionMetaResourceAssembler;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "process-definitions/{id}/meta", produces = MediaTypes.HAL_JSON_VALUE)
public class ProcessDefinitionMetaController {
	
	private final RepositoryService repositoryService;
	private final ProcessDefinitionMetaResourceAssembler resourceAssembler;

    @Autowired
    public ProcessDefinitionMetaController(RepositoryService repositoryService,
    		ProcessDefinitionMetaResourceAssembler resourceAssembler) {
        this.repositoryService = repositoryService;
        this.resourceAssembler = resourceAssembler;
    }
	
	@RequestMapping(method = RequestMethod.GET)
    public ProcessDefinitionMetaResource getProcessDefinitionMetadata(@PathVariable String id) {
    	org.activiti.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        if (processDefinition == null) {
            throw new ActivitiException("Unable to find process definition for the given id:'" + id + "'");
        }
        
        Process process = repositoryService.getBpmnModel(id).getMainProcess();
        List<ProcessDefinitionVariable> variables = getVariables(process);
       
        List<String> users = new ArrayList<String>();
        List<String> groups = new ArrayList<String>();
        List<ProcessDefinitionUserTask> userTasks = new ArrayList<ProcessDefinitionUserTask>();
        List<ProcessDefinitionServiceTask> serviceTasks = new ArrayList<ProcessDefinitionServiceTask>();
        List<FlowElement> flowElementList = (List<FlowElement>) process.getFlowElements();
        for (FlowElement flowElement : flowElementList) {
        	if(flowElement.getClass().equals(UserTask.class))
        	{
        		UserTask userTask = (UserTask) flowElement;
        		ProcessDefinitionUserTask task = new ProcessDefinitionUserTask(
        				userTask.getName(), userTask.getDocumentation());
        		userTasks.add(task);
        		users.addAll(userTask.getCandidateUsers());
        		groups.addAll(userTask.getCandidateGroups());
        	}
        	if(flowElement.getClass().equals(ServiceTask.class))
        	{
        		ServiceTask serviceTask = (ServiceTask) flowElement;
        		ProcessDefinitionServiceTask task = new ProcessDefinitionServiceTask(
        				serviceTask.getName(), serviceTask.getImplementation());
        		serviceTasks.add(task);
        	}
        }
        
        return resourceAssembler.toResource(new ProcessDefinitionMeta(
        		processDefinition.getId(),
        		processDefinition.getName(),
        		processDefinition.getDescription(),
        		processDefinition.getVersion(),
        		users,
        		groups,
        		variables,
        		userTasks,
        		serviceTasks));
	}
	
	private List<ProcessDefinitionVariable> getVariables(Process process)
	{
		List<ProcessDefinitionVariable> variables = new ArrayList<ProcessDefinitionVariable>();
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
        			String name = ee.getAttributeValue(ee.getNamespace(), "variableName");
        			String type = ee.getAttributeValue(ee.getNamespace(), "variableType");
        			ProcessDefinitionVariable variable = new ProcessDefinitionVariable(name, type);
        			variables.add(variable);
        		}
        	}
        }
		return variables;
	}

}
