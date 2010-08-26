package org.activiti.rest.api.cycle;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.ParametrizedAction;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

public class ArtifactActionFormGet extends ActivitiWebScript {

  /**
   * Returns an action's form.
   * 
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model) {
    // Retrieve the artifactId from the request
    String artifactId = getMandatoryString(req, "artifactId");
    String actionName = getMandatoryString(req, "actionName");
    
    // Retrieve session and repo connector
    String cuid = getCurrentUserId(req);

    HttpSession session = ((WebScriptServletRequest) req).getHttpServletRequest().getSession(true);
    RepositoryConnector conn = SessionUtil.getRepositoryConnector(cuid, session);

    // Retrieve the artifact from the repository
    RepositoryArtifact artifact = conn.getRepositoryArtifact(artifactId);

    if(artifact == null) {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "There is no artifact with id '" + artifactId + "'.");
    }
    
    // Retrieve the action and its form
    String form = null;
    for (ParametrizedAction action : artifact.getParametrizedActions()) {
      if (action.getName().equals(actionName)) {
        form = action.getFormAsHtml();
        break;
      }
    }
    
    // Place the form in the response
    if (form != null) {
      model.put("form", form);
    } else {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "There is no form for action '" + actionName + "'.");
    }
  }
}
