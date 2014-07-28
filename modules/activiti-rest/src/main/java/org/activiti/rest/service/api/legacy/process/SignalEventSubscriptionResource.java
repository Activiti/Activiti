package org.activiti.rest.service.api.legacy.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.Execution;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SignalEventSubscriptionResource extends SecuredResource {

	@Post
	public ObjectNode signalEventSubscription(Representation entity) {
		ObjectNode responseJSON = new ObjectMapper().createObjectNode();
		
		String processInstanceId = (String) getRequest().getAttributes().get("processInstanceId");
		String signalName = (String) getRequest().getAttributes().get("signalName");

    if (StringUtils.isEmpty(processInstanceId)) {
      responseJSON.put("success", false);
      responseJSON.put("failureReason", "No process instance is provided");
      return responseJSON;
    }
    
    if (StringUtils.isEmpty(signalName)) {
      responseJSON.put("success", false);
      responseJSON.put("failureReason", "No signal name is provided");
      return responseJSON;
    }
		
		try {
			// check authentication
			if (authenticate() == false) {
			  responseJSON.put("success", false);
        responseJSON.put("failureReason", "Not authenticated");
        return responseJSON;
			}
			
			Map<String, Object> variables = new HashMap<String, Object>();
			if (entity != null) {
  			String signalVariables = entity.getText();
  			if (StringUtils.isNotEmpty(signalVariables)) {
          JsonNode startJSON = new ObjectMapper().readTree(signalVariables);
          variables.putAll(retrieveVariables(startJSON));
  			}
			}
			
			List<Execution> executionList = ActivitiUtil.getRuntimeService()
			    .createExecutionQuery()
			    .signalEventSubscriptionName(signalName)
			    .list();
			
			for (Execution execution : executionList) {
			  if (execution.getProcessInstanceId().equals(processInstanceId)) {
			    if (variables.size() > 0) {
			      ActivitiUtil.getRuntimeService().signalEventReceived(signalName, execution.getId(), variables);
			    } else {
			      ActivitiUtil.getRuntimeService().signalEventReceived(signalName, execution.getId());
			    }
			  }
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
