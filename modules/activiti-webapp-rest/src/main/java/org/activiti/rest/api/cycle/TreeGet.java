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

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.rest.api.cycle.dto.TreeArtifactDto;
import org.activiti.rest.api.cycle.dto.TreeFolderDto;
import org.activiti.rest.api.cycle.dto.TreeNodeDto;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class TreeGet extends ActivitiCycleWebScript {

  // private static Logger log = Logger.getLogger(TreeGet.class.getName());

  @Override
  protected void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    RepositoryNodeCollection rootNodes = this.repositoryService.getChildren("/", "");
    List<TreeFolderDto> tree = new ArrayList<TreeFolderDto>();
    for (RepositoryNode repositoryNode : rootNodes.asList()) {
      tree.add(new TreeFolderDto((RepositoryFolder) repositoryNode));
    }

    String connectorId = req.getString("connectorId");
    String nodeId = req.getString("nodeId");
    if (connectorId != null && nodeId != null) {

      RepositoryNode node = this.repositoryService.getRepositoryNode(connectorId, nodeId);

      TreeNodeDto nodeDto;
      if (node instanceof RepositoryArtifact) {
        nodeDto = new TreeArtifactDto((RepositoryArtifact) node);
      } else {
        nodeDto = new TreeFolderDto((RepositoryFolder) node);
        nodeDto.setExpanded(String.valueOf(Boolean.TRUE));
      }

      String parentFolderId = node.getMetadata().getParentFolderId();
      while (parentFolderId != null && parentFolderId.length() > 0 && !parentFolderId.equals("/")) {
        node = this.repositoryService.getRepositoryNode(connectorId, parentFolderId);
        TreeFolderDto parentNodeDto = new TreeFolderDto((RepositoryFolder) node);
        parentNodeDto.setExpanded(String.valueOf(Boolean.TRUE));

        List<TreeNodeDto> dtoChildren = new ArrayList<TreeNodeDto>();
        for (RepositoryNode currentNode : this.repositoryService.getChildren(connectorId, node.getNodeId()).asList()) {
          if (currentNode.getNodeId().equals(nodeDto.getArtifactId())) {
            dtoChildren.add(nodeDto);
          } else if (currentNode instanceof RepositoryArtifact) {
            dtoChildren.add(new TreeArtifactDto((RepositoryArtifact) currentNode));
          } else {
            dtoChildren.add(new TreeFolderDto((RepositoryFolder) currentNode));
          }
        }
        parentNodeDto.setChildren(dtoChildren);

        parentFolderId = node.getMetadata().getParentFolderId();
        nodeDto = parentNodeDto;
      }

      for (TreeNodeDto treeNode : tree) {
        if (treeNode.getConnectorId().equals(connectorId)) {
          ((TreeFolderDto) treeNode).setChildren(((TreeFolderDto) nodeDto).getChildren());
          treeNode.setExpanded(String.valueOf(Boolean.TRUE));
        }
      }
    }

    model.put("tree", tree);
  }
}
