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

package org.activiti.rest.api.cycle.dto;

import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.impl.processsolution.connector.ProcessSolutionRepositoryNode;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public abstract class TreeNodeDto {

  protected String label;
  protected String connectorId;
  protected String nodeId;
  protected String expanded;
  protected String type;
  protected String vFolderId;

  public TreeNodeDto(RepositoryNode node) {
    this.label = node.getMetadata().getName();
    this.connectorId = node.getConnectorId();
    this.nodeId = node.getNodeId();

    if (node instanceof ProcessSolutionRepositoryNode) {
      ProcessSolutionRepositoryNode processSolutionRepositoryNode = (ProcessSolutionRepositoryNode) node;
      VirtualRepositoryFolder vFolder = processSolutionRepositoryNode.getVirtualRepositoryFolder();
      if (vFolder != null) {
        type = vFolder.getType();
        if (processSolutionRepositoryNode.getWrappedNode() != null) {
          vFolderId = vFolder.getId();
        }
      }
    }
  }

  public TreeNodeDto() {

  }

  public String getLabel() {
    return label;
  }

  public String getConnectorId() {
    return connectorId;
  }

  public String getExpanded() {
    return expanded;
  }

  public void setExpanded(String expanded) {
    this.expanded = expanded;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setConnectorId(String connectorId) {
    this.connectorId = connectorId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getvFolderId() {
    return vFolderId;
  }

  public void setvFolderId(String vFolderId) {
    this.vFolderId = vFolderId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((connectorId == null) ? 0 : connectorId.hashCode());
    result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TreeNodeDto other = (TreeNodeDto) obj;
    if (connectorId == null) {
      if (other.connectorId != null)
        return false;
    } else if (!connectorId.equals(other.connectorId))
      return false;
    if (nodeId == null) {
      if (other.nodeId != null)
        return false;
    } else if (!nodeId.equals(other.nodeId))
      return false;
    return true;
  }

}
