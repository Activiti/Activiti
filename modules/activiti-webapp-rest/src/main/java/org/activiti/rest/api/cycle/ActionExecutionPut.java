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

import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class ActionExecutionPut extends ActivitiCycleWebScript {

  @Override
  protected void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    String connectorId = req.getMandatoryString("connectorId");
    String nodeId = req.getMandatoryString("nodeId");
    String actionId = req.getMandatoryString("actionName");
    String vFolderId = req.getString("vFolderId");

    if (vFolderId != null && vFolderId.length() > 0 && !vFolderId.equals("undefined")) {
      connectorId = "ps-" + processSolutionService.getVirtualRepositoryFolderById(vFolderId).getProcessSolutionId();
      CycleRequestContext.set("vFolderId", vFolderId);
    }

    Map<String, Object> parameters = req.getFormVariables();
    try {
      repositoryService.executeParameterizedAction(connectorId, nodeId, actionId, parameters);
      model.put("result", true);
    } catch (Exception e) {
      // TODO: see whether this makes sense, probably either exception or
      // negative result.
      model.put("result", false);
      throw new RuntimeException(e);
    }

  }

}
