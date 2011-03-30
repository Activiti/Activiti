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
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.cycle.impl.RepositoryNodeCollectionImpl;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;
import org.activiti.rest.api.cycle.dto.TreeFolderDto;
import org.activiti.rest.api.cycle.dto.TreeLeafDto;
import org.activiti.rest.api.cycle.dto.TreeNodeDto;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 * @author daniel.meyer@camunda.com
 */
public class TreeGet extends ActivitiCycleWebScript {

  @Override
  protected void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {

    String treeRootNodeId = req.getString("treeRootNodeId");
    String treeRootConnectorId = req.getString("treeRootConnectorId");

    RepositoryNodeCollection rootNodes = null;
    if (treeRootNodeId != null && treeRootNodeId.length() > 0 && !treeRootNodeId.equals("undefined") && treeRootConnectorId != null
            && treeRootConnectorId.length() > 0 && !treeRootConnectorId.equals("undefined")) {
      // load custom "rootNode"
      List<RepositoryNode> rootNodesList = new ArrayList<RepositoryNode>();
      rootNodesList.add(repositoryService.getRepositoryNode(treeRootConnectorId, treeRootNodeId));
      rootNodes = new RepositoryNodeCollectionImpl(rootNodesList);
    } else {
      // load all roots
      rootNodes = this.repositoryService.getChildren("/", "");
    }
    String treeId = req.getMandatoryString("treeId");
    if (treeId.equals("repo")) {
      rootNodes = filterRepositories(rootNodes);
    } else {
      rootNodes = filterProcessSolutions(rootNodes);
    }
    // load tree
    List<TreeFolderDto> tree = new ArrayList<TreeFolderDto>();
    for (RepositoryNode repositoryNode : rootNodes.asList()) {
      tree.add(new TreeFolderDto((RepositoryFolder) repositoryNode));
    }
    String connectorId = req.getString("connectorId");
    String nodeId = req.getString("nodeId");
    String vFolderId = req.getString("vFolderId");
    if (vFolderId != null && vFolderId.length() > 0) {
      // if vFolder is set, load the tree using the ProcessSolutionConenctor
      VirtualRepositoryFolder virtualRepositoryFolder = processSolutionService.getVirtualRepositoryFolderById(vFolderId);
      connectorId = "ps-" + virtualRepositoryFolder.getProcessSolutionId();
      CycleRequestContext.set("vFolderId", vFolderId);
    }
    if (connectorId != null && nodeId != null) {
      try {
        // try to expand the tree
        expandTree(tree, connectorId, nodeId);
      } catch (Exception e) {
        log.log(Level.WARNING, "Could not expand tree " + e.getMessage(), e);
      }
    }
    model.put("tree", tree);
  }

  private RepositoryNodeCollection filterRepositories(RepositoryNodeCollection rootNodes) {
    List<RepositoryNode> resultList = new ArrayList<RepositoryNode>();
    for (RepositoryNode repositoryNode : rootNodes.asList()) {
      if (!repositoryNode.getConnectorId().startsWith("ps")) {
        resultList.add(repositoryNode);
      }
    }
    return new RepositoryNodeCollectionImpl(resultList);
  }

  private RepositoryNodeCollection filterProcessSolutions(RepositoryNodeCollection rootNodes) {
    List<RepositoryNode> resultList = new ArrayList<RepositoryNode>();
    for (RepositoryNode repositoryNode : rootNodes.asList()) {
      if (repositoryNode.getConnectorId().startsWith("ps")) {
        resultList.add(repositoryNode);
      }
    }
    return new RepositoryNodeCollectionImpl(resultList);
  }

  private void expandTree(List<TreeFolderDto> tree, String connectorId, String nodeId) {
    TreeFolderDto connectorRootNode = null;
    // locate the node for this connector in the tree
    for (TreeFolderDto treeFolderDto : tree) {
      if (treeFolderDto.getConnectorId().equals(connectorId)) {
        connectorRootNode = treeFolderDto;
        connectorRootNode.setExpanded(String.valueOf(Boolean.TRUE));
        break;
      }
    }

    // expand the root node
    expandNode(connectorRootNode, false, connectorId);

    // expand the current node and iteratively its parent nodes until we reach
    // the root node.
    if (!nodeId.equals(connectorRootNode.getNodeId())) {
      RepositoryNode node = repositoryService.getRepositoryNode(connectorId, nodeId);
      TreeNodeDto treeNodeDtoForTheCurrentNode = getTreeNodeDto(node, false);
      if (treeNodeDtoForTheCurrentNode instanceof TreeFolderDto) {
        expandNode((TreeFolderDto) treeNodeDtoForTheCurrentNode, false, connectorId);
        if (((TreeFolderDto) treeNodeDtoForTheCurrentNode).getChildren().size() > 0) {
          treeNodeDtoForTheCurrentNode.setExpanded(String.valueOf(Boolean.TRUE));
        }
      }
      String parentId = node.getMetadata().getParentFolderId();
      while (parentId != null && parentId.length() > 0 && !parentId.equals(connectorRootNode.getNodeId())) {
        RepositoryNode parentNode = repositoryService.getRepositoryNode(connectorId, parentId);
        TreeFolderDto parentTreeNode = (TreeFolderDto) getTreeNodeDto(parentNode, true);
        expandNode(parentTreeNode, false, connectorId);
        parentTreeNode.replaceNode(treeNodeDtoForTheCurrentNode);
        treeNodeDtoForTheCurrentNode = parentTreeNode;
        parentId = parentNode.getMetadata().getParentFolderId();
      }
      connectorRootNode.replaceNode(treeNodeDtoForTheCurrentNode);
    }
  }

  protected void expandNode(TreeFolderDto dto, boolean expandChildren, String connectorId) {
    RepositoryNodeCollection childNodes = repositoryService.getChildren(connectorId, dto.getNodeId());
    List<TreeNodeDto> children = new ArrayList<TreeNodeDto>();
    for (RepositoryNode node : childNodes.asList()) {
      TreeNodeDto childNode = getTreeNodeDto(node, expandChildren);
      children.add(childNode);
    }
    dto.setChildren(children);
  }

  protected TreeNodeDto getTreeNodeDto(RepositoryNode node, boolean expand) {
    TreeNodeDto dto = null;
    if (node instanceof RepositoryArtifact) {
      dto = new TreeLeafDto((RepositoryArtifact) node);
    } else if (node instanceof RepositoryFolder) {
      dto = new TreeFolderDto((RepositoryFolder) node);
      dto.setExpanded(String.valueOf((Boolean) expand));
    }
    return dto;
  }
}
