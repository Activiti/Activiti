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
package org.activiti.cycle.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactLink;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.RepositoryNodeNotFoundException;

/**
 * Cycle service used for accessing repositories.
 * <p />
 * Get an instance of this service by
 * {@link CycleServiceFactory#getRepositoryService()}
 * 
 */
public interface CycleRepositoryService {

  public static class RuntimeConnectorList implements Serializable {

    private static final long serialVersionUID = 1L;
    // the transient field keeps the servlet container from serializing the
    // connectors in the session
    // TODO: needs testing: When do servlet containers serialize/deserialize?
    // Tomcat seems to do it
    // between shutdowns / startups. At the moment I would qualify this as a
    // 'hack' - Daniel Meyer
    public transient List<RepositoryConnector> connectors;
  }

  public void addArtifactLink(RepositoryArtifactLink link);

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

  public void deleteLink(String linkId);

  public List<RepositoryArtifactLink> getArtifactLinks(String sourceConnectorId, String sourceArtifactId);

  /**
   * gets all elements
   */
  public RepositoryNodeCollection getChildren(String connectorId, String folderId) throws RepositoryNodeNotFoundException;

  public Content getContent(String connectorId, String artifactId, String representationName) throws RepositoryNodeNotFoundException;

  public List<RepositoryArtifactLink> getIncomingArtifactLinks(String targetConnectorId, String targetArtifactId);

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
   * return the list of supported {@link ArtifactType}s of this
   * {@link RepositoryConnector} for the given folder. Most conenctors doesn't
   * make any difference between the folders, but some may do.
   */
  public List<ArtifactType> getSupportedArtifactTypes(String connectorId, String folderId);

  /**
   * update artifact content with default {@link ContentRepresentation}
   */
  public void updateContent(String connectorId, String artifactId, Content content) throws RepositoryNodeNotFoundException;

  public void updateContent(String connectorId, String artifactId, String contentRepresentationName, Content content) throws RepositoryNodeNotFoundException;

  /**
   * Some connectors support commit (like SVN), so all pending changes must be
   * committed correctly. If the connector doesn't support committing, this
   * method just does nothing. This means, there is no rollback and you
   * shouldn't rely on a transaction behavior.
   */
  public void commitPendingChanges(String comment);

  public void executeParameterizedAction(String connectorId, String artifactId, String actionId, Map<String, Object> parameters) throws Exception;

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

}
