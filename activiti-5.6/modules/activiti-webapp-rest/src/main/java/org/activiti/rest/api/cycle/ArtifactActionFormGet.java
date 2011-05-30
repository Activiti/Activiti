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

import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class ArtifactActionFormGet extends ActivitiCycleWebScript {

  /**
   * Returns an action's form.
   * 
   * @param req
   *          The webscripts request
   * @param status
   *          The webscripts status
   * @param cache
   *          The webscript cache
   * @param model
   *          The webscripts template model
   */
  @Override
  protected void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    // Retrieve the nodeId from the request
    String connectorId = req.getMandatoryString("connectorId");
    String nodeId = req.getMandatoryString("nodeId");
    String actionId = req.getMandatoryString("actionName");
    String vFolderId = req.getString("vFolderId");

    // Retrieve the artifact from the repository
    if (vFolderId != null && vFolderId.length() > 0) {
      connectorId = "ps-" + processSolutionService.getVirtualRepositoryFolderById(vFolderId).getProcessSolutionId();
      CycleRequestContext.set("vFolderId", vFolderId);
    }

    String form;
    try {
      form = repositoryService.getActionFormTemplate(connectorId, nodeId, actionId);
    } catch (RepositoryNodeNotFoundException e) {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "There is no artifact with id '" + nodeId + "' for connector with id '" + connectorId + "'.");
    }

    // Place the form in the response
    if (form != null) {
      model.put("form", form);
    } else {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "There is no form for action '" + actionId + "'.");
    }
  }
}
