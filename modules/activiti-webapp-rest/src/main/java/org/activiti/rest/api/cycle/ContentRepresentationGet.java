/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.rest.api.cycle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleDefaultMimeType;
import org.activiti.cycle.CycleService;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.impl.CycleServiceImpl;
import org.activiti.cycle.impl.transform.TransformationException;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
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
      model.put("connectorId", connectorId);
      model.put("artifactId", artifactId);

      ContentRepresentation contentRepresentation = artifact.getArtifactType().getContentRepresentation(representationId);
      switch (contentRepresentation.getRenderInfo()) {
      case IMAGE:
      case HTML:
        // For images and HTML we don't need to send the content, the URL will
        // be put together in the UI
        // and the content will be requested via ContentGet.
        break;
      case HTML_REFERENCE:
      case BINARY:
      case CODE:
      case TEXT_PLAIN:
        String content = this.cycleService.getContent(connectorId, artifactId, contentRepresentation.getId()).asString();
        model.put("content", content);
      }
      model.put("renderInfo", contentRepresentation.getRenderInfo().name());
      model.put("contentRepresentationId", contentRepresentation.getId());
      model.put("contentType", contentRepresentation.getMimeType().getContentType());
    } catch (TransformationException e) {
      // Show errors that occur during transformations as HTML in the UI
      model.put("renderInfo", RenderInfo.HTML);
      model.put("contentRepresentationId", "Exception");
      model.put("content", e.getRenderContent());
      model.put("contentType", CycleDefaultMimeType.HTML.getContentType());
    } catch (Exception ex) {
      log.log(Level.WARNING, "Exception while loading content representation", ex);
      // TODO:Better concept how this is handled in the GUI
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      String stackTrace = "Exception while accessing content. Details:\n\n" + sw.toString();
      model.put("exception", stackTrace);
    }
  }
}
