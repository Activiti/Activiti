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

import java.util.Map;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.impl.connector.signavio.transform.TransformationException;
import org.activiti.cycle.impl.mimetype.HtmlMimeType;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class ContentRepresentationGet extends ActivitiCycleWebScript {

  @Override
  protected void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    String connectorId = req.getMandatoryString("connectorId");
    String artifactId = req.getString("artifactId");
    String representationId = req.getString("representationId");

    RepositoryArtifact artifact = repositoryService.getRepositoryArtifact(connectorId, artifactId);

    // Get representation by id to determine whether it is an image...
    try {
      model.put("connectorId", connectorId);
      model.put("artifactId", artifactId);

      ContentRepresentation contentRepresentation = contentService.getContentRepresentation(artifact, representationId);
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
        String content = contentRepresentation.getContent(artifact).asString();
        model.put("content", content);
      }
      model.put("renderInfo", contentRepresentation.getRenderInfo().name());
      model.put("contentRepresentationId", contentRepresentation.getId());
      model.put("contentType", contentRepresentation.getRepresentationMimeType().getName());
    } catch (TransformationException e) {
      // Show errors that occur during transformations as HTML in the UI
      model.put("renderInfo", RenderInfo.HTML);
      model.put("contentRepresentationId", representationId);
      model.put("contentType", CycleApplicationContext.get(HtmlMimeType.class).getName());
    } 
  }
}
