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

/**
 * 
 * @author bernd.ruecker@camunda.com
 */
public class RootFolderInfo extends FolderInfo {

  public RootFolderInfo() {
    // initialize with empty array to avoid lazy loading
    files = new ArrayList<FileInfo>();
    subFolders = new ArrayList<FolderInfo>();
  }

  public void addNewRootFolder(String name, FolderInfo folder) {
    folder.setName(name + "-ROOT");
    subFolders.add(folder);
  }

  @Override
  public void createFile(FileInfo file) {
    throw new UnsupportedRepositoryOpperation("cannot create file in root folder");
  }

  @Override
  public void createFolder(FolderInfo folder) {
    throw new UnsupportedRepositoryOpperation("cannot create folder in root folder");
  }

  @Override
  public void deleteFile(String fileName) {
    throw new UnsupportedRepositoryOpperation("cannot delete file in root folder");
  }

  @Override
  public void deleteFolder(String folderName) {
    throw new UnsupportedRepositoryOpperation("cannot delete folder in root folder");
  }

  public String toString() {
    return "RootFolderInfo [id=" + id + ";name=" + name + ";path=" + path + "]";
  }
}
