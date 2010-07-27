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

import java.util.ArrayList;
import java.util.List;

/**
 * Object for information about a folder. Abstracts from the concrete physical
 * repository (e.g. in Signavio the id is important, in JCR the path.)
 * 
 * @author bernd.ruecker@camunda.com
 */
public class FolderInfo extends ItemInfo {

  protected List<FolderInfo> subFolders = null;
  protected List<FileInfo> files = null;

  public FolderInfo() {
  }

  public FolderInfo(RepositoryConnector connector) {
    super(connector);
  }

  public List<FolderInfo> getSubFolders() {
    if (subFolders == null) {
      loadChildren();
    }
    return subFolders;
  }

  public List<FileInfo> getFiles() {
    if (files == null) {
      loadChildren();
    }
    return files;
  }

  private void loadChildren() {
    subFolders = new ArrayList<FolderInfo>();
    files = new ArrayList<FileInfo>();
    // TODO: Think about if we really want to cache here, since that raises
    // additional problems as well!
    getConnector().loadChildren(this);
  }

  /**
   * create and add a new file to this folder
   */
  public void createFile(FileInfo file) {
    getConnector().createNewFile(this, file);
    getFiles().add(file);
  }

  /**
   * create and add a new folder to this folder
   */
  public void createFolder(FolderInfo folder) {
    getConnector().createNewSubFolder(this, folder);
    getSubFolders().add(folder);
  }

  public String toString() {
    return "FolderInfo [id=" + id + ";name=" + name + ";path=" + path + "]";
  }

  public boolean containsFileWithName(String fileName) {
    return (getFileWithName(fileName) != null);
  }

  public FileInfo getFileWithName(String fileName) {
    for (FileInfo file : getFiles()) {
      if (fileName.equals(file.getName())) {
        return file;
      }
    }
    return null;
  }

  public void deleteFile(String fileName) {
    FileInfo file = getFileWithName(fileName);
    getConnector().deleteFile(this, file);
    getFiles().remove(file);
  }

  public boolean containsFolderWithName(String folderName) {
    return (getFolderWithName(folderName) != null);
  }

  public FolderInfo getFolderWithName(String folderName) {
    for (FolderInfo folder : getSubFolders()) {
      if (folderName.equals(folder.getName())) {
        return folder;
      }
    }
    return null;
  }

  public void deleteFolder(String folderName) {
    FolderInfo folder = getFolderWithName(folderName);
    getConnector().deleteSubFolder(this, folder);
    getSubFolders().remove(folder);
  }
}
