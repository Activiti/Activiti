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
package org.activiti.cycle;

import java.util.Map;

/**
 * 
 * @author bernd.ruecker@camunda.com
 */
public interface RepositoryConnector {

  /**
   * log in given user and return true, if login was successful and false, if
   * the user couldn't be logged in
   */
  public boolean login(String username, String password);

  // /**
  // * get all child nodes of a node with the given url, independent if the
  // * children are folders or artifacts.
  // */
  // public List<RepositoryNode> getChildNodes(String parentId) throws
  // RepositoryNodeNotFoundException;

  /**
   * Return the repository node represented by the provided id.
   * 
   * Consider using {@link #getRepositoryArtifact(String)} or
   * {@link #getRepositoryFolder(String)} if the type of the node is known.
   */
  public RepositoryNode getRepositoryNode(String id) throws RepositoryNodeNotFoundException;

  /**
   * load the {@link RepositoryArtifact} including details
   */
  public RepositoryArtifact getRepositoryArtifact(String id) throws RepositoryNodeNotFoundException;

  /**
   * returns a preview for the artifact if available, otherwiese null is
   * returned. Not every connector must provide a preview for all
   * {@link ArtifactType}s.
   */
  public Content getRepositoryArtifactPreview(String artifactId) throws RepositoryNodeNotFoundException;

  public RepositoryFolder getRepositoryFolder(String id) throws RepositoryNodeNotFoundException;

  /**
   * gets all elements
   */
  public RepositoryNodeCollection getChildren(String id) throws RepositoryNodeNotFoundException;

  // /**
  // * return the list of supported {@link ArtifactType}s of this
  // * {@link RepositoryConnector} for the given folder. Most conenctors doesn't
  // * make any difference between the folders, but some may do.
  // */
  // public List<ArtifactType> getSupportedArtifactTypes(String folderId);

  /**
   * create a new file in the given folder with the default
   * {@link ContentRepresentation}
   * 
   * @param artifactId
   */
  public RepositoryArtifact createArtifact(String parentFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException;

  public RepositoryArtifact createArtifactFromContentRepresentation(String parentFolderId, String artifactName, String artifactType,
          String contentRepresentationName, Content artifactContent) throws RepositoryNodeNotFoundException;

  /**
   * create a new subfolder in the given folder
   */
  public RepositoryFolder createFolder(String parentFolderId, String name) throws RepositoryNodeNotFoundException;

  /**
   * Retunrs the {@link Content} for the provided {@link RepositoryArtifact}-id
   * using the default {@link ContentRepresentation} as returned by
   * {@link #getDefaultContentRepresentation(String)}.
   * 
   * @param artifactId
   *          the id of the {@link RepositoryArtifact} to retreive the
   *          {@link Content} for.
   * @return the {@link Content} for the provided {@link RepositoryArtifact}-id
   * @throws RepositoryNodeNotFoundException
   *           if no {@link RepositoryArtifact} for the provided id exists.
   */
  public Content getContent(String artifactId) throws RepositoryNodeNotFoundException;

  /**
   * update artifact content with default {@link ContentRepresentation}
   */
  public void updateContent(String artifactId, Content content) throws RepositoryNodeNotFoundException;

  public void updateContent(String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException;

  /**
   * deletes the given file from the folder
   */
  public void deleteArtifact(String artifactId) throws RepositoryNodeNotFoundException;

  /**
   * deletes the given subfolder of the parent folder.
   * 
   * TODO: Think about if we need the parent folder as argument of this API
   */
  public void deleteFolder(String folderId) throws RepositoryNodeNotFoundException;

  /**
   * TODO double check the signature
   */
  public void executeParameterizedAction(String artifactId, String actionId, Map<String, Object> parameters) throws Exception;

  /**
   * return true if the connector is currently logged in, returns false,
   * otherwise.
   */
  public boolean isLoggedIn();

  /**
   * Returns the default {@link ContentRepresentation} for a provided
   * {@link RepositoryArtifact}-id.
   * <p />
   * NOTE: The connector implementation must ensure that
   * {@link #getContent(String)} =
   * {@link #getDefaultContentRepresentation(String)}.getContent(String)
   * <p />
   * 
   * @param the
   *          {@link RepositoryArtifact}-id to return the default
   *          {@link ContentRepresentation} for.
   * 
   * @return the default {@link ContentRepresentation} for the provided
   *         {@link RepositoryArtifact}-id
   * 
   * @throws RepositoryNodeNotFoundException
   *           if no {@link RepositoryArtifact} for the provided id exists.
   */
  public ContentRepresentation getDefaultContentRepresentation(String artifactId) throws RepositoryNodeNotFoundException;

  /**
   * must be called before a {@link RepositoryConnector} is configured.
   * 
   * @see #addConfiguration(Map)
   * @see #configurationFinished()
   * 
   */
  public void startConfiguration();

  /**
   * Can be used to configure this {@link RepositoryConnector}. Call
   * {@link #startConfiguration()} before. Can be called multiple times. Call
   * {@link #configurationFinished()} after you are done configuring this
   * connector.
   * 
   * @param configurationValues
   *          a map of configuration values for this connector. Can be called
   *          multiple times.
   * 
   * @see #startConfiguration()
   * @see #configurationFinished()
   */
  public void addConfiguration(Map<String, Object> configurationValues);
  
  /**
   * Add a single configuration entry
   */
  public void addConfigurationEntry(String key, Object value);

  /**
   * called after the configuration of a connector is finished.
   * 
   * @see #startConfiguration()
   * @see #addConfiguration(Map)
   */
  public void configurationFinished();

  /**
   * @return an array of Strings representing the keys allowed in
   *         {@link #addConfiguration(Map)}
   */
  public String[] getConfigurationKeys();

  /**
   * @param connectorId
   *          the id associated with this {@link RepositoryConnector} instance
   */
  public void setId(String connectorId);

  /**
   * returns the id for this {@link RepositoryConnector}
   */
  public String getId();

  /**
   * returns the name for this {@link RepositoryConnector}
   */
  public String getName();

  /**
   * @param name
   *          set the name for this {@link RepositoryConnector}
   */
  public void setName(String name);

  // public String getGlobalId(RepositoryNode node);

}
