package org.activiti.rest.api.cycle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ContentRepresentationGet extends ActivitiWebScript {

  private static Logger log = Logger.getLogger(ContentRepresentationGet.class.getName());

  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    String cuid = req.getCurrentUserId();

    WebScriptRequest wsReq = req.getWebScriptRequest();
    HttpSession session = req.getHttpServletRequest().getSession(true);
    RepositoryConnector conn = SessionUtil.getRepositoryConnector(cuid, session);

    String artifactId = req.getString("artifactId");
    String representationId = req.getString("representationId");
    String restProxyUri = req.getString("restProxyUri");

    RepositoryArtifact artifact = conn.getRepositoryArtifact(artifactId);

    // Get representation by id to determine whether it is an image...
    try {
      for (ContentRepresentation contentRepresentation : artifact.getArtifactType().getContentRepresentations()) {
        if (contentRepresentation.getId().equals(representationId)) {
          if (contentRepresentation.getMimeType().startsWith("image/")) {
            String imageUrl = restProxyUri + "/content?artifactId=" + URLEncoder.encode(artifactId, "UTF-8")
                    + "&content-type=" + URLEncoder.encode(contentRepresentation.getMimeType(), "UTF-8");
            model.put("imageUrl", imageUrl);
          } else {
            String content = conn.getContent(artifactId, contentRepresentation.getId()).asString();
            model.put("content", content);
          }
          model.put("id", contentRepresentation.getId());
          break;
        }
      }
    } catch (Exception ex) {
      // we had a problem with a content representation log and go on,
      // that this will not prevent other representations to be shown
      log.log(Level.WARNING, "Exception while loading content representation", ex);

      // TODO:Better concept how this is handled in the GUI
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      String stackTrace = "Exception while accessing content. Details:\n\n" + sw.toString();
      model.put("exception", stackTrace);
    }
  }

}
