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
package org.activiti.impl.cycle.connect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.impl.cycle.connect.api.ArtifactType;
import org.activiti.impl.cycle.connect.api.ContentRepresentationProvider;
import org.activiti.impl.cycle.connect.api.actions.FileAction;

/**
 * Central class to register {@link ArtifactType}s, associated {@link FileAction}s and {@link ContentLink}s.
 * 
 * @author bernd.ruecker@camunda.com
 */
public class RepositoryRegistry {

  private static Map<String, ArtifactType> registeredFileTypes = new HashMap<String, ArtifactType>();
  
  private static Map<String, List<Class< ? extends FileAction>>> registeredFileActions = new HashMap<String, List<Class< ? extends FileAction>>>();
  private static Map<String, List<ContentRepresentationProvider>> registeredContentLinkProviders = new HashMap<String, List<ContentRepresentationProvider>>();
  
  private static Map<String, Class< ? extends FileAction>> defaultFileActions = new HashMap<String, Class< ? extends FileAction>>();

  public static void registerFileType(ArtifactType ft) {
    registeredFileTypes.put(ft.getTypeIdentifier(), ft);
  }

  public static Collection<String> getFileTypeIdentifiers() {
    return registeredFileTypes.keySet();
  }

  public static ArtifactType getFileTypeByIdentifier(String fileTypeIdentifier) {
    return registeredFileTypes.get(fileTypeIdentifier);
  }

  public static void registerFileAction(String fileTypeIdentifier, Class< ? extends FileAction> action) {
    registerFileAction(fileTypeIdentifier, action, false);
  }

  public static void registerContentLinkProvider(String fileTypeIdentifier, ContentRepresentationProvider provider) {
    if (registeredContentLinkProviders.containsKey(fileTypeIdentifier)) {
    	registeredContentLinkProviders.get(fileTypeIdentifier).add(provider);
	} 
    else {
		ArrayList<ContentRepresentationProvider> list = new ArrayList<ContentRepresentationProvider>();
		list.add(provider);
		registeredContentLinkProviders.put(fileTypeIdentifier, list);
	}
  }

  @Deprecated /** use the one without default flag */  
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

  @Deprecated
  public static Class< ? extends FileAction> getDefaultActionForFileType(String fileTypeName) {
    return defaultFileActions.get(fileTypeName);
  }

  public static List<Class< ? extends FileAction>> getActionsForFileType(String fileTypeName) {
    if (registeredFileActions.containsKey(fileTypeName)) {
      return registeredFileActions.get(fileTypeName);
    } else {
      return new ArrayList<Class< ? extends FileAction>>();
    }
  }

  public static List<ContentRepresentationProvider> getContentLinkProviders(String fileTypeName) {
	    if (registeredContentLinkProviders.containsKey(fileTypeName)) {
	      return registeredContentLinkProviders.get(fileTypeName);
	    } else {
	      return new ArrayList<ContentRepresentationProvider>();
	    }
	  }

  public static ContentRepresentationProvider getContentLinkProvider(String fileTypeName, String providerName) {
    for (ContentRepresentationProvider provder : getContentLinkProviders(fileTypeName)) {
      if (provder.getName().equals(providerName)) {
        return provder;
      }
    }
    throw new RepositoryException("Couldn't find content provider '" + providerName + "' for filetype '" + fileTypeName + "'");
  }

}
