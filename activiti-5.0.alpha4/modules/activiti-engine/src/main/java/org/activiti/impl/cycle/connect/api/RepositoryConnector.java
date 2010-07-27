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

import java.util.List;

/**
 * 
 * @author bernd.ruecker@camunda.com
 */
public interface RepositoryConnector {

  /**
   * get Root folder
   */
  public FolderInfo getRootFolder();

   /**
   * Get child folders for the given parent folder. If parentFolder is null
   * sub folders for the root folder are returned.
   */
   public List<FolderInfo> getChildFolders(FolderInfo parentFolder);
  
  // /**
  // * get files for the given parent folder. If parentFolder is null
  // * files for the root folder are returned.
  // */
  // public List<FileInfo> getFiles(FolderInfo parentFolder);

  /**
   * load sub folders and files for the given folder and add them to the
   * {@link FolderInfo} object, which is returned again.
   */
  public FolderInfo loadChildren(FolderInfo folder);

  /**
   * create a new file in the given folder
   */
  public void createNewFile(FolderInfo folderInfo, FileInfo file);

  /**
   * deletes the given file from the folder
   */
  public void deleteFile(FolderInfo folderInfo, FileInfo file);

  /**
   * create a new subfolder in the given folder
   */
  public void createNewSubFolder(FolderInfo parentFolder, FolderInfo subFolder);

  /**
   * deletes the given subfolder of the parent folder.
   * 
   * TODO: Think about if we need the parent folder as argument of this API
   */
  public void deleteSubFolder(FolderInfo parentFolder, FolderInfo subFolder);

}
