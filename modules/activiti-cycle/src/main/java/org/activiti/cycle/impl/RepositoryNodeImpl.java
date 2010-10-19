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

import org.activiti.cycle.RepositoryException;
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
  
  private String currentPath;

  public RepositoryNodeImpl(String connectorId, String nodeId) {
    this.connectorId = connectorId;
    this.nodeId = nodeId;
    // Creating a repositoryNodeImpl is done by the original connector, hence
    // the nodeId is already the correct current path:
    this.currentPath = nodeId;
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
  public String getOriginalNodeId() {
    return nodeId;
  }

  public String getGlobalUniqueId() {
    return getConnectorId() + "/" + getOriginalNodeId();
  }
  
  public String getCurrentPath() {
    if (currentPath == null) {
      throw new RepositoryException("current path is unset for " + this + "! Check implementation.");
    }
    return currentPath;
  }
  
  public void setCurrentPath(String currentPath) {
    this.currentPath = currentPath;
  }
  
  public void addNewRootToCurrentPath(String rootName) {
    if (!getCurrentPath().startsWith("/")) {
      throw new RepositoryException("RepositoryNode id doesn't start with a slash, which is considered invalid: '" + getCurrentPath() + "' in repository '"
              + getConnectorId() + "'");
    } else {
      if (rootName.startsWith("/")) {
        setCurrentPath(rootName + getCurrentPath());
      } else {
        setCurrentPath("/" + rootName + getCurrentPath());
      }
    }    
  }

  
}
