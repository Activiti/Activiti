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
import java.util.logging.Level;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.rest.api.cycle.dto.TreeFolderDto;
import org.activiti.rest.api.cycle.dto.TreeLeafDto;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class ChildNodesGet extends ActivitiCycleWebScript {

  @Override
  protected void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {

    String nodeId = req.getMandatoryString("nodeId");
    String connectorId = req.getMandatoryString("connectorId");
    String vFolderId = req.getString("vFolderId");
    if (vFolderId != null && vFolderId.length() > 0) {
      VirtualRepositoryFolder virtualRepositoryFolder = processSolutionService.getVirtualRepositoryFolderById(vFolderId);
      if (virtualRepositoryFolder != null) {
        connectorId = "ps-" + virtualRepositoryFolder.getProcessSolutionId();
        CycleRequestContext.set("vFolderId", vFolderId);
      }
    }
    try {
      RepositoryNodeCollection children = repositoryService.getChildren(connectorId, nodeId);
      List<TreeFolderDto> folders = new ArrayList<TreeFolderDto>();
      List<TreeLeafDto> leafs = new ArrayList<TreeLeafDto>();
      for (RepositoryArtifact artifact : children.getArtifactList()) {
        leafs.add(new TreeLeafDto(artifact));
      }
      for (RepositoryFolder folder : children.getFolderList()) {
        folders.add(new TreeFolderDto(folder));
      }
      model.put("files", leafs);
      model.put("folders", folders);
      return;

    } catch (RepositoryException e) {
      log.log(Level.SEVERE, "Cannot load children for id '" + nodeId + "'", e);
      // TODO: how can we let the user know what went wrong without breaking the
      // tree?
      // throwing a HTTP 500 here will cause the tree to load the node forever.
      // throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
      // "exception.message");
    }

    // provide empty list as default
    model.put("files", new ArrayList<RepositoryArtifact>());
    model.put("folders", new ArrayList<RepositoryFolder>());
  }
}
