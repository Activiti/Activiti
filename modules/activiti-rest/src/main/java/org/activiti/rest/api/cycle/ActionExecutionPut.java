package org.activiti.rest.api.cycle;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

public class ActionExecutionPut extends ActivitiWebScript {

  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {

    String artifactId = req.getMandatoryString("artifactId");
    String actionId = req.getMandatoryString("actionName");
    
    String cuid = req.getCurrentUserId();
    
    HttpSession session = req.getHttpSession();
    RepositoryConnector conn = SessionUtil.getRepositoryConnector(cuid, session);
    
    ActivitiRequest.ActivitiWebScriptBody body = req.getBody();
    Map<String, Object> parameters = req.getFormVariables(body);
    
    try {
      conn.executeParameterizedAction(artifactId, actionId, parameters);
      model.put("result", true);
    } catch (Exception e) {
      // TODO: see whether this makes sense, probably either exception or negative result.
      model.put("result", false);
      throw new RuntimeException(e);
    }
    
  }

}
