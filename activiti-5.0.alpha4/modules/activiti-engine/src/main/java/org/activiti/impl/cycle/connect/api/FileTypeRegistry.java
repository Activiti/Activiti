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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.impl.cycle.connect.api.actions.FileAction;

/**
 * Central class to register {@link FileType}s and associated {@link FileAction}s.
 * 
 * @author bernd.ruecker@camunda.com
 */
public class FileTypeRegistry {

  private static Map<String, FileType> registeredFileTypes = new HashMap<String, FileType>();
  private static Map<String, List<Class< ? extends FileAction>>> registeredFileActions = new HashMap<String, List<Class< ? extends FileAction>>>();
  private static Map<String, Class< ? extends FileAction>> defaultFileActions = new HashMap<String, Class< ? extends FileAction>>();

  public static void registerFileType(FileType ft) {
    registeredFileTypes.put(ft.getTypeIdentifier(), ft);
  }

  public static Collection<String> getFileTypeIdentifiers() {
    return registeredFileTypes.keySet();
  }

  // public static FileType getFileTypeByName(String fileTypeName) {
  //
  // }

  public static FileType getFileTypeByIdentifier(String fileTypeIdentifier) {
    return registeredFileTypes.get(fileTypeIdentifier);
  }

  public static void registerFileAction(String fileTypeIdentifier, Class< ? extends FileAction> action) {
    registerFileAction(fileTypeIdentifier, action, false);
  }

  public static void registerFileAction(String fileTypeIdentifier, Class< ? extends FileAction> action, boolean isDefault) {
    if (registeredFileActions.containsKey(fileTypeIdentifier)) {
      registeredFileActions.get(fileTypeIdentifier).add(action);
    } else {
      ArrayList<Class< ? extends FileAction>> list = new ArrayList<Class< ? extends FileAction>>();
      list.add(action);
      registeredFileActions.put(fileTypeIdentifier, list);
    }
    // register default action. If a default action was already registered it is overwritten.
    if (isDefault) {
      defaultFileActions.put(fileTypeIdentifier, action);
    }
  }

  public static Class< ? extends FileAction> getDefaultActionForFileType(String fileTypeName) {
    return defaultFileActions.get(fileTypeName);
  }

  public static List<Class< ? extends FileAction>> getRegisteredActionsForFileType(String fileTypeName) {
    if (registeredFileActions.containsKey(fileTypeName)) {
      return registeredFileActions.get(fileTypeName);
    } else {
      return new ArrayList<Class< ? extends FileAction>>();
    }
  }

}
