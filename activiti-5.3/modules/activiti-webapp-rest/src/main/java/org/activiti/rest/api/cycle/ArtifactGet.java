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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.action.DownloadContentAction;
import org.activiti.rest.api.cycle.dto.DownloadActionView;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 * @author Bernd Rücker
 */
public class ArtifactGet extends ActivitiCycleWebScript {

  @Override
  protected void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {

    // Retrieve the artifactId from the request
    String connectorId = req.getMandatoryString("connectorId");
    String artifactId = req.getString("artifactId");
    String restProxyUri = req.getString("restProxyUri");

    // Retrieve the artifact from the repository
    RepositoryArtifact artifact = repositoryService.getRepositoryArtifact(connectorId, artifactId);

    List<String> contentRepresentations = new ArrayList<String>();
    for (ContentRepresentation representation : contentService.getContentRepresentations(artifact)) {
      contentRepresentations.add(representation.getId());
    }

    model.put("contentRepresentations", contentRepresentations);

    model.put("actions", pluginService.getParameterizedActions(artifact));

    // Create downloadContentView DTOs
    List<DownloadActionView> downloads = new ArrayList<DownloadActionView>();
    for (DownloadContentAction action : pluginService.getDownloadContentActions(artifact)) {
      try {
        String url = restProxyUri + "content?connectorId=" + URLEncoder.encode(connectorId, "UTF-8") + "&artifactId=" + URLEncoder.encode(artifactId, "UTF-8")
                + "&contentRepresentationId=" + URLEncoder.encode(action.getContentRepresentation().getId(), "UTF-8");
        downloads.add(new DownloadActionView(action.getId(), url, action.getContentRepresentation().getRepresentationMimeType().getContentType(), action
                .getContentRepresentation().getId()));
      } catch (UnsupportedEncodingException e) {
        // should never be reached as long as we use UTF-8, which is valid in
        // java on all platforms
        throw new RuntimeException(e);
      }
    }

    model.put("downloads", downloads);
    model.put("links", pluginService.getArtifactOpenLinkActions(artifact));
    model.put("artifactId", artifact.getNodeId());
    model.put("connectorId", artifact.getConnectorId());
  }
}
