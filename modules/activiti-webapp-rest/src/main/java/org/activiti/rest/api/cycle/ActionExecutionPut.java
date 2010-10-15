package org.activiti.rest.api.cycle;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.db.CycleServiceDbXStreamImpl;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

public class ActionExecutionPut extends ActivitiWebScript {

  // private CycleService cycleService;
  private RepositoryConnector repositoryConnector;

  private void init(ActivitiRequest req) {
    String cuid = req.getCurrentUserId();

    HttpSession session = req.getHttpServletRequest().getSession(true);
    // this.cycleService = SessionUtil.getCycleService();
    this.repositoryConnector = CycleServiceDbXStreamImpl.getRepositoryConnector(cuid, session);
  }
  
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    init(req);
    
    String artifactId = req.getMandatoryString("artifactId");
    String actionId = req.getMandatoryString("actionName");
    
    ActivitiRequest.ActivitiWebScriptBody body = req.getBody();
    Map<String, Object> parameters = req.getFormVariables(body);
    
    try {
      this.repositoryConnector.executeParameterizedAction(artifactId, actionId, parameters);
      model.put("result", true);
    } catch (Exception e) {
      // TODO: see whether this makes sense, probably either exception or negative result.
      model.put("result", false);
      throw new RuntimeException(e);
    }
    
  }

}
