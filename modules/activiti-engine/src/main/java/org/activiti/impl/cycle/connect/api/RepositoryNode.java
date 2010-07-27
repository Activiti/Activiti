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
package org.activiti.impl.cycle.connect.api;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.impl.cycle.connect.RepositoryException;

/**
 * Superclass for the composite of folders and files. Holds a reference to the
 * API used to query sub folders and files in order to enable lazy loading of
 * them.
 * 
 * @author bernd.ruecker@camunda.com
 */
public class RepositoryNode implements Serializable {

  private static final long serialVersionUID = 1L;

  protected static Logger log = Logger.getLogger(RepositoryNode.class.getName());

  /**
   * local part of the URL for node. This is the one and only used unique
   * identifier for the node used by the client and the repository API. All
   * details can be queried by this URL later on, by adding the repo base URL to
   * the beginning.
   */
  private String id;
  
  /**
   * The URL used in the repo internally to query the artifact
   */
  private String sourceSystemUrl;

  /**
   * The url used in the client (e.g. GUI) for this artifact
   */
  private String clientUrl;

  /**
   * flag to indicate if the object was queried with all details or just the
   * "header" information like the name, which required lazy loading.
   * 
   * Eager fetching or lazy loading can be decided by the Connector, since this
   * is pretty different depending on the technology.
   */
  private boolean detailsFetched = false;

  private RepositoryNodeMetadata metadata = new RepositoryNodeMetadata();

  private transient RepositoryConnector connector;

  public RepositoryNode() {
  }

  public RepositoryNode(RepositoryConnector connector) {
    this.connector = connector;
  }

  public RepositoryConnector getConnector() {
    if (connector == null) {
      throw new RepositoryException("Item " + this + " is not connected to any repository");
    }
    return connector;
  }

  public String toString() {
    return this.getClass() + " [id=" + id + ";metadata=" + metadata + "]";
  }

  public boolean isDetailsFetched() {
    return detailsFetched;
  }

  public RepositoryNodeMetadata getMetadata() {
    return metadata;
  }
  
  public Map<String, String> getMetadataAsMap() {
    return metadata.getAsStringMap();
  }
  
  public String getSourceSystemUrl() {
    return sourceSystemUrl;
  }

  public void setSourceSystemUrl(String sourceSystemUrl) {
    this.sourceSystemUrl = sourceSystemUrl;
  }

  public String getClientUrl() {
    return clientUrl;
  }

  public void setClientUrl(String clientUrl) {
    this.clientUrl = clientUrl;
  }

  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
