package org.activiti.rest.api.process;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.Execution;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageEventSubscriptionResource extends SecuredResource {

	@Post
	public ObjectNode signalEventSubscription(Representation entity) {
		ObjectNode responseJSON = new ObjectMapper().createObjectNode();
		
		String processInstanceId = (String) getRequest().getAttributes().get("processInstanceId");
		String messageName = (String) getRequest().getAttributes().get("messageName");

    if (StringUtils.isEmpty(processInstanceId)) {
      responseJSON.put("success", false);
      responseJSON.put("failureReason", "No process instance is provided");
      return responseJSON;
    }
    
    if (StringUtils.isEmpty(messageName)) {
      responseJSON.put("success", false);
      responseJSON.put("failureReason", "No message name is provided");
      return responseJSON;
    }
		
		try {
			// check authentication
			if (!authenticate()) {
			  responseJSON.put("success", false);
        responseJSON.put("failureReason", "Not authenticated");
        return responseJSON;
			}
			
			Map<String, Object> variables = new HashMap<String, Object>();
			if (entity != null) {
  			String eventVariables = entity.getText();
  			if (StringUtils.isNotEmpty(eventVariables)) {
          JsonNode startJSON = new ObjectMapper().readTree(eventVariables);
          variables.putAll(retrieveVariables(startJSON));
  			}
			}
			
			List<Execution> executionList = ActivitiUtil.getRuntimeService()
			    .createExecutionQuery()
			    .messageEventSubscriptionName(messageName)
			    .list();
			
			for (Execution execution : executionList) {
			  if (execution.getProcessInstanceId().equals(processInstanceId)) {
			    if (variables.size() > 0) {
			      ActivitiUtil.getRuntimeService().messageEventReceived(messageName, execution.getId(), variables);
			    } else {
			      ActivitiUtil.getRuntimeService().messageEventReceived(messageName, execution.getId());
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
			throw new ActivitiException("Failed to trigger message event for process instance id " + processInstanceId, e);
		}

	}
}