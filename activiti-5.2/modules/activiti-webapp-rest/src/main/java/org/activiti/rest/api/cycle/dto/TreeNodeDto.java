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

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public abstract class TreeNodeDto {

  protected final String label;
  protected final String connectorId;
  protected final String artifactId;
  protected String expanded;

  public TreeNodeDto(RepositoryNode node) {
    this.label = node.getMetadata().getName();
    this.connectorId = node.getConnectorId();
    this.artifactId = node.getNodeId();
  }

  public String getLabel() {
    return label;
  }

  public String getConnectorId() {
    return connectorId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getExpanded() {
    return expanded;
  }

  public void setExpanded(String expanded) {
    this.expanded = expanded;
  }

}
