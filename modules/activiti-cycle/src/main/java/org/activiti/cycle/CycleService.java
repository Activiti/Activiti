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
   * Log in user to all registered repositories
   * 
   * TODO: (Nils Preusker, 20.10.2010) this needs to be reviewed, we are
   * currently expecting one user for all repos...
   */
  public boolean login(String username, String password);

  /**
   * Some connectors support commit (like SVN), so all pending changes must be
   * committed correctly. If the connector doesn't support committing, this
   * method just does nothing. This means, there is no rollback and you
   * shouldn't rely on a transaction behavior.
   */
  public void commitPendingChanges(String connectorId, String comment);

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
  public RepositoryArtifact createArtifact(String connectorId, String containingFolderId, String artifactName, String artifactType, Content artifactContent)
          throws RepositoryNodeNotFoundException;

  public RepositoryArtifact createArtifactFromContentRepresentation(String connectorId, String containingFolderId, String artifactName, String artifactType,
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

  public List<RepositoryArtifactLink> getArtifactLinks(String sourceArtifactId, Long sourceRevision);
  public List<RepositoryArtifactLink> getArtifactLinks(String sourceConnectorId, String sourceArtifactId);
  public List<RepositoryArtifactLink> getArtifactLinks(String sourceArtifactId, Long sourceRevision, String type);

  public void deleteLink(long linkId);

  /**
   * add tag for the given node id. Tags are identified by their string names
   */
  public void addTag(String nodeId, String tagName);

  /**
   * add tag for the given node id and specify an alias which can be used in the
   * GUI later on when showing the tag to the user
   */
  public void addTag(String nodeId, String tagName, String alias);

  /**
   * delete the
   */
  public void deleteTag(String nodeId, String tagName);

  /**
   * returns all {@link CycleTag}s for the {@link RepositoryNode} with the given
   * id. Returns an empty list if not tags are available. Please note that
   * different alias for tag names lead to different {@link CycleTag} objects.
   */
  public List<CycleTag> getTags(String nodeId) throws RepositoryNodeNotFoundException;

  /**
   * get all available tags for the system in order to show them in the GUI (as
   * folder, tag cloud, ...)
   */
  public List<CycleTag> getAllTags();

  /**
   * get all available tags for the system in order to show them in the GUI (as
   * folder, tag cloud, ...) but ignore the alias settings, meaning the same tag
   * names with different alias are merged together (which normally make sense
   * for the top level GUI)
   */
  public List<CycleTag> getAllTagsIgnoreAlias();

}
