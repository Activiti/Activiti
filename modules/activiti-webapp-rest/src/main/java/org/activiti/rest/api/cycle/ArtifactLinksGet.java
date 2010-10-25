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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.CycleService;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.impl.CycleServiceImpl;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class ArtifactLinksGet extends ActivitiWebScript {

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

    List<RepositoryArtifactLink> links = this.cycleService.getArtifactLinks(connectorId, artifactId);
    for (RepositoryArtifactLink link : links) {
      if (link.getSourceElementId()==null) {
        link.setSourceElementId("");
      }
      if (link.getSourceElementName()==null) {
        link.setSourceElementName("");
      }
      if (link.getTargetElementId()==null) {
        link.setTargetElementId("");
      }
      if (link.getTargetElementName()==null) {
        link.setTargetElementName("");
      }
      if (link.getLinkType()==null) {
        link.setLinkType("");
      }
      if (link.getComment()==null) {
        link.setComment("");
      }
    }

    model.put("links", links);
  }

}
