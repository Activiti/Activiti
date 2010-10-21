package org.activiti.rest.api.cycle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleService;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.CycleServiceImpl;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

public class ContentRepresentationGet extends ActivitiWebScript {

  private static Logger log = Logger.getLogger(ContentRepresentationGet.class.getName());

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
    String artifactId = req.getString("artifactId");
    String representationId = req.getString("representationId");

    RepositoryArtifact artifact = this.cycleService.getRepositoryArtifact(connectorId, artifactId);

    // Get representation by id to determine whether it is an image...
    try {
      ContentRepresentation contentRepresentation = artifact.getArtifactType().getContentRepresentation(representationId);
      switch (contentRepresentation.getRenderInfo()) {
      case IMAGE:
      case HTML:
        // For images and HTML we don't need to send the content, the URL will be put together in the UI
        // and the content will be requested via ContentGet.
        break;
      case HTML_REFERENCE:
      case BINARY:
      case CODE:
      case TEXT_PLAIN:
        String content = this.cycleService.getContent(connectorId, artifactId, contentRepresentation.getId()).asString();
        model.put("content", content);
      }

      model.put("connectorId", connectorId);
      model.put("artifactId", artifactId);
      model.put("renderInfo", contentRepresentation.getRenderInfo().name());
      model.put("contentRepresentationId", contentRepresentation.getId());
      model.put("contentType", contentRepresentation.getMimeType().getContentType());

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
