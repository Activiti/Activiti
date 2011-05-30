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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.action.RepositoryArtifactOpenLinkAction;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.impl.processsolution.connector.ProcessSolutionArtifact;
import org.activiti.rest.api.cycle.dto.UrlActionDto;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiRequestObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

/**
 * Creates a new {@link RepositoryArtifact} through the {@link CycleService}
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class ArtifactPost extends ActivitiCycleWebScript {

  @Override
  protected void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    FormField file = null;
    try {
      file = ((WebScriptServletRequest) req.getWebScriptRequest()).getFileField("file");
    } catch (NullPointerException npe) {
      // We can just ignore this exception since an empty "file" field is valid.
    }

    ActivitiRequestObject obj = req.getBody();

    String connectorId = req.getMandatoryString(obj, "connectorId");
    String parentFolderId = req.getMandatoryString(obj, "parentFolderId");
    String artifactName = req.getMandatoryString(obj, "artifactName");
    String linkToNodeId = req.getOptionalString(obj, "linkToNodeId");
    String linkToConnectorId = req.getOptionalString(obj, "linkToConnectorId");
    String linkType = req.getOptionalString(obj, "linkType");

    // TODO: set a meaningful value for artifactType
    String artifactType = "";

    artifactName = getNonExistingArtifactName(artifactName, connectorId, parentFolderId);

    Content artifactContent = new Content();
    if (file != null) {
      artifactContent.setValue(file.getInputStream());
    }
    RepositoryArtifact createdArtifact = null;

    try {

      if (artifactContent.isNull()) {
        createdArtifact = repositoryService.createEmptyArtifact(connectorId, parentFolderId, artifactName, artifactType);
      } else {
        createdArtifact = repositoryService.createArtifact(connectorId, parentFolderId, artifactName, artifactType, artifactContent);
      }
      model.put("result", true);
      if (createdArtifact instanceof ProcessSolutionArtifact) {
        model.put("vFolderId", ((ProcessSolutionArtifact) createdArtifact).getVirtualRepositoryFolder().getId());
      }
      model.put("artifact", createdArtifact);
      List<UrlActionDto> link = new ArrayList<UrlActionDto>();
      for (RepositoryArtifactOpenLinkAction openLinkAction : pluginService.getArtifactOpenLinkActions(createdArtifact)) {
        link.add(new UrlActionDto(openLinkAction.getId(), openLinkAction.getUrl().toString()));
      }
      model.put("links", link);

      if (linkToNodeId != null && linkToNodeId.length() > 0 && !linkToNodeId.equals("undefined") && linkToConnectorId != null && linkToConnectorId.length() > 0
              && !linkToConnectorId.equals("undefined")) {
        RepositoryArtifact targetArtifact = repositoryService.getRepositoryArtifact(linkToConnectorId, linkToNodeId);
        RepositoryArtifactLinkEntity newLink = new RepositoryArtifactLinkEntity();
        newLink.setLinkType(linkType);
        newLink.setSourceArtifact(createdArtifact);
        newLink.setTargetArtifact(targetArtifact);
        repositoryService.addArtifactLink(newLink);
      }

    } catch (Exception e) {
      model.put("result", false);
    }
  }
  protected String getNonExistingArtifactName(String artifactName, String connectorId, String parentFolderId) {
    String name = "";
    for (char c : artifactName.toCharArray()) {
      if (Character.isLetter(c) || Character.isDigit(c) || "_".equals(String.valueOf(c)) || ".".equals(String.valueOf(c))) {
        name += c;
      }
    }
    artifactName = name;
    String uniqueName = artifactName;
    int counter = 0;
    boolean exists = true;
    while (exists) {
      // test if exists:
      RepositoryNodeCollection collection = repositoryService.getChildren(connectorId, parentFolderId);
      if (collection.getArtifactByName(uniqueName) == null) {
        exists = false;
      } else {
        uniqueName = counter + artifactName;
        counter++;
      }
    }
    return uniqueName;
  }
}
