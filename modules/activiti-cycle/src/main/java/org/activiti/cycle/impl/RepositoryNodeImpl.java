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
package org.activiti.cycle.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeMetadata;

/**
 * Superclass for the composite of folders and files. Holds a reference to the
 * API used to query sub folders and files in order to enable lazy loading of
 * them.
 * 
 * @author bernd.ruecker@camunda.com
 */
public class RepositoryNodeImpl implements RepositoryNode, Serializable {

  private static final long serialVersionUID = 1L;

  protected Logger log = Logger.getLogger(this.getClass().getName());
  
  private final RepositoryNodeMetadata metadata = new RepositoryNodeMetadataImpl();

  private final String connectorId;

  private final String nodeId;
  
  public RepositoryNodeImpl(String connectorId, String nodeId) {
    this.connectorId = connectorId;
    this.nodeId = nodeId;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [connectorId=" + connectorId + ";nodeid=" + nodeId + ";metadata=" + metadata + "]";
  }

  public RepositoryNodeMetadata getMetadata() {
    return metadata;
  }

  public Map<String, String> getMetadataAsMap() {
    return metadata.getAsStringMap();
  }

  /**
   * {@inheritDoc}
   */
  public String getConnectorId() {
    return connectorId;
  }

  /**
   * {@inheritDoc}
   */
  public String getNodeId() {
    return nodeId;
  }

  public String getGlobalUniqueId() {
    return getConnectorId() + "/" + getNodeId();
  }
  
}
