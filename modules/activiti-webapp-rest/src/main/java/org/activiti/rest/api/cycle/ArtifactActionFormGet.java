package org.activiti.rest.api.cycle;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.CycleService;
import org.activiti.cycle.ParameterizedAction;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.CycleServiceImpl;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

public class ArtifactActionFormGet extends ActivitiWebScript {

  private CycleService cycleService;

  private void init(ActivitiRequest req) {
    String cuid = req.getCurrentUserId();

    HttpSession session = req.getHttpServletRequest().getSession(true);
    this.cycleService = CycleServiceImpl.getCycleService(cuid, session);
  }

  /**
   * Returns an action's form.
   * 
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    init(req);
    
    // Retrieve the artifactId from the request
    String connectorId = req.getMandatoryString("connectorId");
    String artifactId = req.getMandatoryString("artifactId");
    String actionId = req.getMandatoryString("actionName");

    // Retrieve the artifact from the repository
    RepositoryArtifact artifact = this.cycleService.getRepositoryArtifact(connectorId, artifactId);

    if (artifact == null) {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "There is no artifact with id '" + artifactId + "' for connector with id '" + connectorId + "'.");
    }

    // Retrieve the action and its form
    String form = null;
    for (ParameterizedAction action : artifact.getArtifactType().getParameterizedActions()) {
      if (action.getId().equals(actionId)) {
        form = action.getFormAsHtml();
        break;
      }
    }

    // Place the form in the response
    if (form != null) {
      model.put("form", form);
    } else {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "There is no form for action '" + actionId + "'.");
    }
  }
}
