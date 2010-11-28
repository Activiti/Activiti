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

import java.util.List;
import java.util.Map;

/**
 * This is the central entry point for Activiti Cycle. The service provides the
 * possibility to store and load user configurations (which then contains
 * {@link RepositoryConnector}s) and to do global actions not tied to a single
 * repository like Tags, Links and so on...
 */
public interface CycleService {

  /**
   * Log in to the repository configured for the given connector with the
   * provided user name and password.
   * 
   * @param username
   *          the username to log in
   * @param password
   *          the password to log in
   * @param connectorId
   *          the id of the repository-connector to use
   */
  public boolean login(String username, String password, String connectorId);

  /**
   * Some connectors support commit (like SVN), so all pending changes must be
   * committed correctly. If the connector doesn't support committing, this
   * method just does nothing. This means, there is no rollback and you
   * shouldn't rely on a transaction behavior.
   */
  public void commitPendingChanges(String comment);

  /**
   * load the {@link RepositoryArtifact} including details
   */
  public RepositoryArtifact getRepositoryArtifact(String connectorId, String artifactId) throws RepositoryNodeNotFoundException;

  /**
   * returns a preview for the artifact if available, otherwiese null is
   * returned. Not every connector must provide a preview for all
   * {@link ArtifactType}s.
   */
  public Content getRepositoryArtifactPreview(String connectorId, String artifactId) throws RepositoryNodeNotFoundException;

  public RepositoryFolder getRepositoryFolder(String connectorId, String folderId) throws RepositoryNodeNotFoundException;

  /**
   * gets all elements
   */
  public RepositoryNodeCollection getChildren(String connectorId, String folderId) throws RepositoryNodeNotFoundException;

  /**
   * return the list of supported {@link ArtifactType}s of this
   * {@link RepositoryConnector} for the given folder. Most conenctors doesn't
   * make any difference between the folders, but some may do.
   */
  public List<ArtifactType> getSupportedArtifactTypes(String connectorId, String folderId);

  /**
   * create a new file in the given folder with the default
   * {@link ContentRepresentation}
   * 
   * @param artifactId
   */
  public RepositoryArtifact createArtifact(String connectorId, String parentFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException;

  public RepositoryArtifact createArtifactFromContentRepresentation(String connectorId, String parentFolderId, String artifactName, String artifactType,
          String contentRepresentationId, Content artifactContent) throws RepositoryNodeNotFoundException;

  /**
   * create a new subfolder in the given folder
   */
  public RepositoryFolder createFolder(String connectorId, String parentFolderId, String name) throws RepositoryNodeNotFoundException;

  public Content getContent(String connectorId, String artifactId, String representationName) throws RepositoryNodeNotFoundException;

  /**
   * update artifact content with default {@link ContentRepresentation}
   */
  public void updateContent(String connectorId, String artifactId, Content content) throws RepositoryNodeNotFoundException;

  public void updateContent(String connectorId, String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException;

  /**
   * deletes the given file from the folder
   */
  public void deleteArtifact(String connectorId, String artifactId) throws RepositoryNodeNotFoundException;

  /**
   * deletes the given subfolder of the parent folder.
   * 
   * TODO: Think about if we need the parent folder as argument of this API
   */
  public void deleteFolder(String connectorId, String folderId) throws RepositoryNodeNotFoundException;
  public void executeParameterizedAction(String connectorId, String artifactId, String actionId, Map<String, Object> parameters) throws Exception;

  public void addArtifactLink(RepositoryArtifactLink link);

  public List<RepositoryArtifactLink> getArtifactLinks(String sourceConnectorId, String sourceArtifactId);

  public List<RepositoryArtifactLink> getIncomingArtifactLinks(String targetConnectorId, String targetArtifactId);

  public void deleteLink(String linkId);

  /**
   * add tag for the given node id and specify an alias which can be used in the
   * GUI later on when showing the tag to the user
   */
  public void addTag(String connectorId, String nodeId, String tagName, String alias);

  public List<String> getTags(String connectorId, String nodeId);

  public List<RepositoryNodeTag> getRepositoryNodeTags(String connectorId, String nodeId);

  /**
   * sets provided tags to the given artifact, this means it should reset the
   * previous tags for that artifact!
   * 
   * Additionally it does some magic for you:
   * <ul>
   * <li>checks for every tag whether it is empty (doesn't create it if that is
   * the case)</li>
   * <li>checks whether the tag already exists, CycleService should worry about
   * duplicate exceptions etc.</li>
   * </ul>
   */
  public void setTags(String connectorId, String nodeId, List<String> tags);

  /**
   * delete the tag
   */
  public void deleteTag(String connectorId, String nodeId, String tagName);

  /**
   * get all tag names matching the given pattern. This can be used to find
   * already used tags to resuse them
   */
  public List<String> getSimiliarTagNames(String tagNamePattern);

  /**
   * get all available tags for the system in order to show them in the GUI (as
   * folder, tag cloud, ...)
   */
  public CycleTagContent getTagContent(String name);

  public List<CycleTagContent> getRootTags();

  /**
   * returns a list of available connector configurations
   */
  public Map<String, String> getAvailableConnectorConfiguatations();

  /**
   * stores a new configuration
   */
  public void updateConfiguration(Map<String, List<Map<String, String>>> connectorConfigMap, String currentUserId);

  public Map<String, String> getConfigurationFields(String configurationClazzName);

  public Map<String, List<String>> getConfiguredConnectors(String currentUserId);

  public Map<String, String> getConfigurationValues(String connectorConfigurationId, String currentUserId);


}
