package org.activiti.rest.service.api.legacy.process;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

/**
 * @author Bilal Farraj <bfarraj@gmail.com>
 * 	
 */
public class ProcessInstanceSignalExecutionResource extends SecuredResource {

	@Post
	public ObjectNode completeReceiveTask(Representation entity) {
		ObjectNode responseJSON = new ObjectMapper().createObjectNode();
		
		String processInstanceId = (String) getRequest().getAttributes().get("processInstanceId");

    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("No process instance is provided");
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

			// extract activity id
			String activityId = (String) variables.remove("activityId");

			if(activityId == null){
				responseJSON.put("success", false);
				responseJSON.put("failureReason", "Request is missing activity id");
				return responseJSON;
			}
				
			RuntimeService runtimeService = ActivitiUtil.getRuntimeService();
			Execution execution = runtimeService.createExecutionQuery()
					  .processInstanceId(processInstanceId)
					  .activityId(activityId)
					  .singleResult();
			
			// signal receive task and attach variables if available
			if (variables.size() > 0) {
			  runtimeService.signal(execution.getId(), variables);
			} else {
			  runtimeService.signal(execution.getId());
			}

			// set up and return response message
			responseJSON.put("success", true);
			return responseJSON;
		} catch (Exception e) {
		  if(e instanceof ActivitiException) {
		    throw (ActivitiException) e;
		  }
			throw new ActivitiException("Failed to signal receive task for process instance id " + processInstanceId, e);
		}
	}
	
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
}
