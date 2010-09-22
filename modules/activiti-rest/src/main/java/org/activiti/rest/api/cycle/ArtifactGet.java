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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.ContentType;
import org.activiti.cycle.DownloadContentAction;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.rest.api.cycle.dto.ContentView;
import org.activiti.rest.api.cycle.dto.DownloadActionView;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

/**
 * @author Nils Preusker
 * @author ruecker
 */
public class ArtifactGet extends ActivitiWebScript {

  private static Logger log = Logger.getLogger(TreeGet.class.getName());

  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    // Retrieve the artifactId from the request
    String artifactId = req.getString("artifactId");

    // Retrieve session and repo connector
    String cuid = req.getCurrentUserId();

    WebScriptRequest wsReq = req.getWebScriptRequest();
    HttpSession session = req.getHttpSession();
    RepositoryConnector conn = SessionUtil.getRepositoryConnector(cuid, session);

    // Retrieve the artifact from the repository
    RepositoryArtifact artifact = conn.getRepositoryArtifact(artifactId);

    List<ContentView> contentViews = new ArrayList<ContentView>();
    for (ContentRepresentation representation : artifact.getArtifactType().getContentRepresentations()) {
      try {
        if (representation.getMimeType().equals(ContentType.TEXT) || representation.getMimeType().equals(ContentType.XML)
                || representation.getMimeType().equals(ContentType.HTML) || representation.getMimeType().equals(ContentType.JAVASCRIPT)) {
          String content = conn.getContent(artifactId, representation.getId()).asString();
          contentViews.add(new ContentView(representation.getMimeType(), representation.getId(), content));
        } else if (representation.getMimeType().startsWith("image/")) {
          String url = wsReq.getServerPath() + wsReq.getContextPath() + "/service/content?artifactId=" + URLEncoder.encode(artifactId, "UTF-8") + "&content-type="
                  + URLEncoder.encode(representation.getMimeType(), "UTF-8");
          contentViews.add(new ContentView(representation.getMimeType(), representation.getId(), url));
        }
      } catch (Exception ex) {
        // we had a problem with a content representation
        // log and go on, that this will not prevent other representations to be
        // shown
        log.log(Level.WARNING, "Exception while loading content representation", ex);

        // TODO:Better concept how this is handled in the GUI
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stackTrace = "Exception while accessing content. Details:\n\n" + sw.toString();

        contentViews.add(new ContentView(ContentType.TEXT, representation.getId(), stackTrace));
      }
    }

    model.put("actions", artifact.getArtifactType().getParameterizedActions());
    
    // Create downloadContentView DTOs
    List<DownloadActionView> downloads = new ArrayList<DownloadActionView>();
    for (DownloadContentAction action : artifact.getArtifactType().getDownloadContentActions()) {
      try {
        String url = wsReq.getServerPath() + wsReq.getContextPath() + "/service/content?artifactId=" + URLEncoder.encode(artifactId, "UTF-8") + "&content-type="
                + URLEncoder.encode(action.getContentRepresentation().getMimeType(), "UTF-8");
        downloads.add(new DownloadActionView(action.getId(), url, action.getContentRepresentation().getMimeType(), action.getContentRepresentation().getId()));
      } catch (UnsupportedEncodingException e) {
        // should never be reached as long as we use UTF-8, which is valid in
        // java on all platforms
        throw new RuntimeException(e);
      }
    }
    model.put("downloads", downloads);

    model.put("links", artifact.getOutgoingLinks());

    model.put("artifactId", artifact.getId());
    model.put("contentViews", contentViews);

  }
}
