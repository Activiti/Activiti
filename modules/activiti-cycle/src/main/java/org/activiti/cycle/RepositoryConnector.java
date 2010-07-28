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

/**
 * 
 * @author bernd.ruecker@camunda.com
 */
public interface RepositoryConnector {
  
  public boolean login(String username, String password);

  // /**
  // * get Root folder
  // */
  // public RepositoryFolder getRootFolder();

  // /**
  // * Get child folders for the given parent folder. If parentFolder is null
  // * sub folders for the root folder are returned.
  // */
  // public List<RepositoryFolder> getChildFolders(RepositoryFolder
  // parentFolder);

  /**
   * get all child nodes of a node with the given url, independent if the
   * children are folders or artifacts. No details are prefetched as default.
   */
  public List<RepositoryNode> getChildNodes(String parentId);

  // /**
  // * get all child nodes of a node with the given url, independent if the
  // * children are folders or artifacts.
  // *
  // * With fetch details the client can indicate, that all details should be
  // * fetched, default should be false or let the repository decide if that can
  // * be done efficiently
  // */
  // public List<RepositoryNode> getChildNodes(String parentId, boolean
  // fetchDetails);

  /**
   * load all details for a {@link RepositoryNode} (if that wasn't prefetched,
   * which is indicated in a node with a flag).
   */
  public RepositoryNode getNodeDetails(String id);
  
  // where to get contentType
  public ContentRepresentation getContent(String nodeId, String representationName);


  // /**
  // * get files for the given parent folder. If parentFolder is null
  // * files for the root folder are returned.
  // */
  // public List<FileInfo> getFiles(FolderInfo parentFolder);

  // /**
  // * load sub folders and files for the given folder and add them to the
  // * {@link RepositoryFolder} object, which is returned again.
  // */
  // public RepositoryFolder loadChildren(RepositoryFolder folder);

  /**
   * create a new file in the given folder
   */
  public void createNewFile(String folderId, RepositoryArtifact file);

  /**
   * deletes the given file from the folder
   */
  public void deleteArtifact(String artifactId);

  /**
   * create a new subfolder in the given folder
   */
  public void createNewSubFolder(String parentFolderId, RepositoryFolder subFolder);

  /**
   * deletes the given subfolder of the parent folder.
   * 
   * TODO: Think about if we need the parent folder as argument of this API
   */
  public void deleteSubFolder(String subFolderId);

}
