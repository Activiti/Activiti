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
import java.util.logging.Level;

import org.activiti.impl.cycle.connect.api.actions.FileAction;

/**
 * Information about a file (or more general: any artifact contained in the
 * repository)
 * 
 * @author bernd.ruecker@camunda.com
 */
public class FileInfo extends ItemInfo {

  private String textContent;
  private byte[] binaryContent;

  // TODO: Think about file types, associated actions and so on. What impact
  // does it have on the content? ...?
  private FileType fileType;

  private transient List<FileAction> cachedFileActions;
  private transient FileAction cachedDefaultFileAction;

  public FileInfo() {
  }

  public FileInfo(RepositoryConnector connector) {
    super(connector);
  }

  public List<FileAction> getActions() {
    if (getFileType() == null) {
      return new ArrayList<FileAction>();
    }

    if (cachedFileActions == null) {
      cachedFileActions = new ArrayList<FileAction>();
      for (Class< ? extends FileAction> clazz : getRegisteredActionTypes()) {
        try {
          FileAction action = clazz.newInstance();
          action.setFile(this);
          cachedFileActions.add(action);

          // check if default and if yes, remember it
          if (isDefaultAction(clazz)) {
            cachedDefaultFileAction = action;
          }
        } catch (Exception ex) {
          log.log(Level.SEVERE, "couldn't create file action of class " + clazz, ex);
        }
      }
    }

    log.fine("Actions for file type " + getFileType().getName() + " requested, returning " + cachedFileActions.size() + " actions.");

    return cachedFileActions;
  }

  public FileAction getDefaultFileAction() {
    if (cachedDefaultFileAction == null) {
      // lazy loading of action definitions if not already done
      getActions();
    }

    log.info("Default actions for file type " + getFileType() + " requested, returning "
            + (cachedDefaultFileAction == null ? "null" : cachedDefaultFileAction.getName()));

    return cachedDefaultFileAction;
  }

  public boolean isDefaultAction(Class< ? extends FileAction> actionType) {
    if (fileType != null && fileType.getDefaultAction() != null) {
      return fileType.getDefaultAction().equals(actionType);
    } else {
      return false;
    }
  }

  public List<Class< ? extends FileAction>> getRegisteredActionTypes() {
    if (fileType != null) {
      return fileType.getRegisteredActions();
    } else {
      return new ArrayList<Class< ? extends FileAction>>();
    }
  }

  public String getTextContent() {
    return textContent;
  }
  public void setTextContent(String textContent) {
    this.textContent = textContent;
  }
  public byte[] getBinaryContent() {
    return binaryContent;
  }
  public void setBinaryContent(byte[] binaryContent) {
    this.binaryContent = binaryContent;
  }

  public FileType getFileType() {
    return fileType;
  }

  public void setFileType(FileType fileType) {
    this.fileType = fileType;
  }

  public String toString() {
    return "FileInfo [id=" + id + ";name=" + name + ";path=" + path + "]";
  }

}
