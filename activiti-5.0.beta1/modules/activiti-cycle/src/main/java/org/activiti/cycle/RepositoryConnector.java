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

import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

/**
 * 
 * @author bernd.ruecker@camunda.com
 */
public interface RepositoryConnector {

  
  public RepositoryConnectorConfiguration getConfiguration();

  /**
   * log in given user and return true, if login was successful and false, if
   * the user couldn't be logged in
   */
  public boolean login(String username, String password);

  /**
   * get all child nodes of a node with the given url, independent if the
   * children are folders or artifacts. No details are prefetched as default.
   */
  public List<RepositoryNode> getChildNodes(String parentId) throws RepositoryNodeNotFoundException;
  
  /**
   * load the {@link RepositoryArtifact} including details
   */
  public RepositoryArtifact getRepositoryArtifact(String id) throws RepositoryNodeNotFoundException;

  public RepositoryFolder getRepositoryFolder(String id) throws RepositoryNodeNotFoundException;

  // TODO: Think about getRepositoryNode method which returns the node
  // independent of the type, but currentlyx this is a problem with the Signavio
  // Connector where we have to know what we want to query
  
  // /**
  // * load a {@link RepositoryNode} independent if it is a
  // * {@link RepositoryArtifact} or {@link RepositoryFolder}
  // */
  // public RepositoryNode getRepositoryNode(String id) throws
  // RepositoryNodeNotFoundException;

  public Content getContent(String nodeId, String representationName) throws RepositoryNodeNotFoundException;

  /**
   * create a new file in the given folder
   */
  public void createNewArtifact(String containingFolderId, RepositoryArtifact artifact, Content artifactContent) throws RepositoryNodeNotFoundException;

  public void modifyArtifact(RepositoryArtifact artifact, ContentRepresentationDefinition artifactContent) throws RepositoryNodeNotFoundException;

  /**
   * deletes the given file from the folder
   */
  public void deleteArtifact(String artifactId) throws RepositoryNodeNotFoundException;

  /**
   * create a new subfolder in the given folder
   */
  public void createNewSubFolder(String parentFolderId, RepositoryFolder subFolder) throws RepositoryNodeNotFoundException;

  /**
   * deletes the given subfolder of the parent folder.
   * 
   * TODO: Think about if we need the parent folder as argument of this API
   */
  public void deleteSubFolder(String subFolderId) throws RepositoryNodeNotFoundException;

  /**
   * Some connectors support commit (like SVN), so all pending changes must be
   * committed correctly. If the connector doesn't support committing, this
   * method just does nothing. This means, there is no rollback and you
   * shouldn't rely on a transaction behavior.
   */
  public void commitPendingChanges(String comment);
}
