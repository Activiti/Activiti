package org.activiti.rest.api.process;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

/**
 * @author Franco Lombardo
 * 	
 */
public class ProcessInstanceSetVariablesResource extends SecuredResource {

	@Post
	public ObjectNode setVariables(Representation entity) {
		ObjectNode responseJSON = new ObjectMapper().createObjectNode();
		
		String processInstanceId = (String) getRequest().getAttributes().get("processInstanceId");

    if (processInstanceId == null) {
      throw new ActivitiException("No process instance is provided");
    }
		
		try {
			// check authentication
			if (authenticate() == false)
				return null;
			
			// extract request parameters
			Map<String, Object> variables = new HashMap<String, Object>();
			String startParams = entity.getText();
			if (StringUtils.isNotEmpty(startParams)) {
  			JsonNode startJSON = new ObjectMapper().readTree(startParams);
  			variables.putAll(retrieveVariables(startJSON));
			}

			RuntimeService runtimeService = ActivitiUtil.getRuntimeService();
			Execution execution = runtimeService.createExecutionQuery()
					  .processInstanceId(processInstanceId)
					  .singleResult();
	    
			if (execution == null) {
	      throw new ActivitiException("Process not found");
	    }
			
			// signal receive task and attach variables if available
			if (variables.size() > 0) {
			  runtimeService.setVariables(execution.getId(), variables);
			}

			// set up and return response message
			responseJSON.put("success", true);
			return responseJSON;
		} catch (Exception e) {
			throw new ActivitiException("Failed to set variables for process instance id " + processInstanceId, e);
		}

	}
}