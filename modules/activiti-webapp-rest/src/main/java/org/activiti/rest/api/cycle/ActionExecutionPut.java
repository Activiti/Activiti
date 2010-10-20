package org.activiti.rest.api.cycle;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.CycleService;
import org.activiti.cycle.impl.CycleServiceImpl;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

public class ActionExecutionPut extends ActivitiWebScript {

   private CycleService cycleService;

  private void init(ActivitiRequest req) {
    String cuid = req.getCurrentUserId();

    HttpSession session = req.getHttpServletRequest().getSession(true);
    this.cycleService = CycleServiceImpl.getCycleService(cuid, session);
  }
  
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    init(req);
    
    String connectorId = req.getMandatoryString("connectorId");
    String artifactId = req.getMandatoryString("artifactId");
    String actionId = req.getMandatoryString("actionName");
    
    ActivitiRequest.ActivitiWebScriptBody body = req.getBody();
    Map<String, Object> parameters = req.getFormVariables(body);
    
    try {
      this.cycleService.executeParameterizedAction(connectorId, artifactId, actionId, parameters);
      model.put("result", true);
    } catch (Exception e) {
      // TODO: see whether this makes sense, probably either exception or negative result.
      model.put("result", false);
      throw new RuntimeException(e);
    }
    
  }

}
