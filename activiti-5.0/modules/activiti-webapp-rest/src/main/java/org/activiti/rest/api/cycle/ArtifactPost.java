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

import org.activiti.cycle.Content;
import org.activiti.cycle.CycleService;
import org.activiti.cycle.RepositoryArtifact;
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
    FormField file = ((WebScriptServletRequest) req.getWebScriptRequest()).getFileField("file");

    ActivitiRequestObject obj = req.getBody();

    String connectorId = req.getMandatoryString(obj, "connectorId");
    String parentFolderId = req.getMandatoryString(obj, "parentFolderId");
    String artifactName = req.getMandatoryString(obj, "artifactName");
    // TODO: what are the possible types and where can I get them/ how can I
    // visualize them in the UI?
    String artifactType = "";

    Content artifactContent = new Content();
    artifactContent.setValue(file.getInputStream());
    try {
      this.cycleService.createArtifact(connectorId, parentFolderId, artifactName, artifactType, artifactContent);
      model.put("result", true);
    } catch (Exception e) {
      model.put("result", false);
    }
  }
}
