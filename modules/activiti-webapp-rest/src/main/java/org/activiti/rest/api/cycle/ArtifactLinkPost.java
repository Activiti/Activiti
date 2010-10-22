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

import javax.servlet.http.HttpSession;

import org.activiti.cycle.CycleService;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.impl.CycleServiceImpl;
import org.activiti.cycle.impl.RepositoryArtifactLinkImpl;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class ArtifactLinkPost extends ActivitiWebScript {

  private CycleService cycleService;

  private void init(ActivitiRequest req) {
    String cuid = req.getCurrentUserId();

    HttpSession session = req.getHttpServletRequest().getSession(true);
    this.cycleService = CycleServiceImpl.getCycleService(cuid, session);
  }

  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    init(req);

    ActivitiRequest.ActivitiWebScriptBody body = req.getBody();

    String srcConnectorId = req.getMandatoryString(body, "connectorId");
    String srcArtifactId = req.getMandatoryString(body, "artifactId");

    // String srcElementName = req.getMandatoryString("sourceElementName");
    // String srcElementId = req.getMandatoryString("sourceElementId");

    // String tgtElementName = req.getMandatoryString("targetElementName");
    // String tgtElementId = req.getMandatoryString("targetElementId");

    String tgtConnectorId = req.getMandatoryString(body, "targetConnectorId");
    String tgtArtifactId = req.getMandatoryString(body, "targetArtifactId");

    RepositoryArtifact srcArtifact = cycleService.getRepositoryArtifact(srcConnectorId, srcArtifactId);
    RepositoryArtifact tgtArtifact = cycleService.getRepositoryArtifact(tgtConnectorId, tgtArtifactId);

    RepositoryArtifactLink link = new RepositoryArtifactLinkImpl(null, srcArtifact, "", "", tgtArtifact, "", "");

    try {
      this.cycleService.addArtifactLink(link);
      model.put("result", true);
    } catch (Exception e) {
      // TODO: see whether this makes sense, probably either exception or
      // negative result.
      model.put("result", false);
      throw new RuntimeException(e);
    }
  }
}
