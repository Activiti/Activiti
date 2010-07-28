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

import java.util.ArrayList;
import java.util.List;

/**
 * A folder in the repository
 * 
 * @author bernd.ruecker@camunda.com
 */
public class RepositoryFolder extends RepositoryNode {

  private List<RepositoryNode> children = null;

  public RepositoryFolder() {
  }

  public RepositoryFolder(RepositoryConnector connector) {
    super(connector);
  }
  
  public List<RepositoryFolder> getSubFolders() {
    ArrayList<RepositoryFolder> list = new ArrayList<RepositoryFolder>();
    for (RepositoryNode node : getChildren()) {
      if (node instanceof RepositoryFolder)
        list.add((RepositoryFolder)node);
    }
    return list;
  }

  public List<RepositoryArtifact> getArtifacts() {
    ArrayList<RepositoryArtifact> list = new ArrayList<RepositoryArtifact>();
    for (RepositoryNode node : getChildren()) {
      if (node instanceof RepositoryArtifact)
        list.add((RepositoryArtifact) node);
    }
    return list;
  }

   private List<RepositoryNode> getChildren() {
     // skip cache for now
    // if (children == null) {
      // TODO: Think about if we really want to cache here, since that raises
      // additional problems as well!
    children = getConnector().getChildNodes(getId());
    // }
    return children;
  }

  /**
   * create and add a new file to this folder
   */
  public void createFile(RepositoryArtifact file) {
    getConnector().createNewFile(getId(), file);
    // getFiles().add(file);
  }

  /**
   * create and add a new folder to this folder
   */
  public void createFolder(RepositoryFolder folder) {
    getConnector().createNewSubFolder(getId(), folder);
    // getSubFolders().add(folder);
  }

  public boolean containsFileWithName(String fileName) {
    return (getArtifactWithName(fileName) != null);
  }

  public RepositoryArtifact getArtifactWithName(String fileName) {
    for (RepositoryArtifact file : getArtifacts()) {
      if (fileName.equals(file.getMetadata().getName())) {
        return file;
      }
    }
    return null;
  }

  // public void deleteFile(String fileName) {
  // RepositoryArtifact file = getArtifactWithName(fileName);
  // getConnector().deleteArtifact(artifactUrl)(this, file);
  // getFiles().remove(file);
  // }

  public boolean containsFolderWithName(String folderName) {
    return (getFolderWithName(folderName) != null);
  }

  public RepositoryFolder getFolderWithName(String folderName) {
    for (RepositoryFolder folder : getSubFolders()) {
      if (folderName.equals(folder.getMetadata().getName())) {
        return folder;
      }
    }
    return null;
  }

  // public void deleteFolder(String folderName) {
  // RepositoryFolder folder = getFolderWithName(folderName);
  // getConnector().deleteSubFolder(this, folder);
  // getSubFolders().remove(folder);
  // }
}
