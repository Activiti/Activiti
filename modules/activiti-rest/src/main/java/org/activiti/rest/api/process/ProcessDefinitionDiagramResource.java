package org.activiti.rest.api.process;

import java.io.InputStream;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

public class ProcessDefinitionDiagramResource extends SecuredResource {

	@Get
	public InputRepresentation getDiagram() {
		if (authenticate() == false)
			return null;

		String processDefinitionId = (String) getRequest().getAttributes().get("processDefinitionId");

		if (processDefinitionId == null) {
			throw new ActivitiIllegalArgumentException("No process definition id provided");
		}

		RepositoryService repositoryService = ActivitiUtil.getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
		    .processDefinitionId(processDefinitionId).singleResult();

		if (processDefinition == null) {
			throw new ActivitiObjectNotFoundException("Process definition " + processDefinitionId + " could not be found", ProcessDefinition.class);
		}

		if (processDefinition.getDiagramResourceName() == null) {
			throw new ActivitiObjectNotFoundException("Diagram resource could not be found", String.class);
		}
		final InputStream definitionImageStream = repositoryService.getResourceAsStream(
		    processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName());

		if (definitionImageStream == null) {
			throw new ActivitiObjectNotFoundException("Diagram resource could not be found", String.class);
		}
		return new InputRepresentation(definitionImageStream, MediaType.IMAGE_PNG);
	}

}
