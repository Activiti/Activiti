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
package org.activiti.cycle.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.ContentProvider;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CreateUrlAction;
import org.activiti.cycle.CycleDefaultMimeType;
import org.activiti.cycle.DownloadContentAction;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.ParameterizedAction;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactOpenLinkAction;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;

/**
 * The type specifies the type of an artifact, e.g. Signavio model, jpdl process
 * model, text file, word document, ...
 * 
 * Linked to this type {@link ContentRepresentationDefinition}s for the GUI and
 * {@link ArtifactAction}s are defined.
 * 
 * TODO: Think about hierarchy
 * 
 * @author bernd.ruecker@camunda.com
 */
public class ArtifactTypeImpl implements ArtifactType {

  private String id;
  
  private MimeType mimeType;

  /**
   * {@link List} of registered {@link ContentRepresentation}s of this
   * {@link ArtifactType}
   */
  private List<ContentRepresentation> contentRepresentationList;
  
  private ContentRepresentation defaultContentRepresentation;
  
  /**
   * {@link Map} with {@link ContentRepresentation} names as key and
   * {@link ContentProvider}s as values
   */
  private Map<String, ContentProvider> contentProviderMap;
  
  private List<ParameterizedAction> parameterizedActions;

  private List<CreateUrlAction> openUrlActions;

  private List<DownloadContentAction> downloadContentActions;
  
  private Long revision;

  public ArtifactTypeImpl(String id, CycleDefaultMimeType mimeType) {
    this.id = id;
    this.mimeType = mimeType;
    this.contentRepresentationList = new ArrayList<ContentRepresentation>();
    this.contentProviderMap = new HashMap<String, ContentProvider>();
    this.parameterizedActions = new ArrayList<ParameterizedAction>();
    this.openUrlActions = new ArrayList<CreateUrlAction>();
    this.downloadContentActions = new ArrayList<DownloadContentAction>();
    this.revision = 0l;
  }

  public ArtifactTypeImpl(String id, MimeType mimeType, List<ContentRepresentation> contentRepresentationList, ContentRepresentation defaultContentRepresentation,
          Map<String, ContentProvider> contentProviderMap, List<ParameterizedAction> parameterizedActions, List<CreateUrlAction> openUrlActions,
          List<DownloadContentAction> downloadContentActions, Long revision) {
    this.id = id;
    this.mimeType = mimeType;
    this.contentRepresentationList = contentRepresentationList;
    this.defaultContentRepresentation = defaultContentRepresentation;
    this.contentProviderMap = contentProviderMap;
    this.parameterizedActions = parameterizedActions;
    this.openUrlActions = openUrlActions;
    this.downloadContentActions = downloadContentActions;
    this.revision = revision;
  }

  public String getId() {
    return id;
  }
  
  public MimeType getMimeType() {
    return this.mimeType;
  }
  
  public ContentRepresentation getContentRepresentation(String id) {
    for (ContentRepresentation cr : getContentRepresentations()) {
      if (id.equals(cr.getId())) {
        return cr;
      }
    }
    throw new RepositoryException("Couldn't find ContentRepresentation with id '" + id + "' in artifact type " + this);
  }

  public List<ContentRepresentation> getContentRepresentations() {
    return contentRepresentationList;
  }

  public ContentRepresentation getDefaultContentRepresentation() {
    return defaultContentRepresentation;
  }  

  public ContentProvider getContentProvider(String contentRepresentationId) {
    ContentProvider provider = contentProviderMap.get(contentRepresentationId);
    if (provider == null) {
      throw new RepositoryException("Couldn't find or load ContentRepresentation '" + contentRepresentationId + "' for artifact type " + this);
    }
    return provider;
  }

//  public void initializeActions() {
//    cachedFileActions = new ArrayList<ArtifactAction>();
//    if (getArtifactType() != null) {
//      cachedFileActions = new ArrayList<ArtifactAction>();
//
//      for (Class< ? extends ArtifactAction> clazz : artifactType.getRegisteredActions()) {
//        try {
//          ArtifactAction action = clazz.newInstance();
//          action.setArtifact(this);
//          cachedFileActions.add(action);
//        } catch (Exception ex) {
//          log.log(Level.SEVERE, "couldn't create file action of class " + clazz, ex);
//        }
//      }
//
//      cachedFileActions.addAll(createDownloadContentActions());
//
//      log.fine("Actions for artifact '" + getId() + "' with type " + getArtifactType().getName() + " requested, returning " + cachedFileActions.size()
//              + " actions.");
//    } else {
//      log.fine("No artifact type set for artifact '" + getId() + "'. Don't return any actions.");
//    }
//
//  }
//
//  private List<ArtifactAction> getRegisteredActions() {
//    if (cachedFileActions == null) {
//      initializeActions();
//    }
//
//    return cachedFileActions;
//  }
//
//  public List<DownloadContentAction> createDownloadContentActions() {
//    // TODO: Think about a better handling of initialization of providers and
//    // representation definitions
//    // For the moment just query the list beforehand to make sure it is
//    // initialized
//    getContentRepresentationDefinitions();
//
//    ArrayList<DownloadContentAction> actions = new ArrayList<DownloadContentAction>();
//
//    for (ContentRepresentationProvider provider : getContentRepresentationProviders()) {
//      if (provider.isContentDownloadable()) {
//        actions.add(new DownloadContentAction(this, provider.getContentRepresentationName()));
//      }
//    }
//
//    return actions;
//  }
//
//  public List<ParametrizedAction> getParametrizedActions() {
//    ArrayList<ParametrizedAction> actions = new ArrayList<ParametrizedAction>();
//    for (ArtifactAction action : getRegisteredActions()) {
//      if (action instanceof ParametrizedAction) {
//        actions.add((ParametrizedAction) action);
//      }
//    }
//    return actions;
//  }
//  
//  public ParameterizedAction getParameterizedAction(String name) {
//
//  }
//
//  public List<OpenUrlAction> getOpenUrlActions() {
//    ArrayList<OpenUrlAction> actions = new ArrayList<OpenUrlAction>();
//    for (ArtifactAction action : getRegisteredActions()) {
//      if (action instanceof OpenUrlAction) {
//        actions.add((OpenUrlAction) action);
//      }
//    }
//    return actions;
//  }
//
//  public List<DownloadContentAction> getDownloadContentActions() {
//    ArrayList<DownloadContentAction> actions = new ArrayList<DownloadContentAction>();
//    for (ArtifactAction action : getRegisteredActions()) {
//      if (action instanceof DownloadContentAction) {
//        actions.add((DownloadContentAction) action);
//      }
//    }
//    return actions;
//  }

  // How can we make that generic?
  // public List<ArtifactAction> getActionsOfType(Class< ? extends
  // ArtifactAction> actionClass) {
  // ArrayList<ArtifactAction> actions = new ArrayList<ArtifactAction>();
  // for (ArtifactAction action : getActions()) {
  // if (actionClass.isAssignableFrom(action.getClass())) {
  // actions.add(action);
  // }
  // }
  // return actions;
  // }

  
//
//  /**
//   * execute the action with the given name and the given parameters.
//   * 
//   * Only {@link ParametrizedAction}s can be executed with parameters, if you
//   * try to execute another action type, a {@link RepositoryException} class is
//   * thrown
//   */
//  public void executeAction(String name, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception {
//    StringBuffer actionNames = new StringBuffer();
//    for (ArtifactAction action : getRegisteredActions()) {
//      if (action.getName().equals(name)) {
//        if (action instanceof ParametrizedAction) {
//          ((ParametrizedAction) action).execute(parameters);
//          return;
//        } else {
//          throw new RepositoryException("cannot execute action '" + name + "' with parameters, because it is not a ParametrizedAction");
//        }
//      } else {
//        if (actionNames.length() > 0) {
//          actionNames.append(", ");
//        }
//        actionNames.append(action.getName());
//      }
//    }
//    throw new RepositoryException("Action '" + name + "' not found, cannot be executed. Existing actions are: " + actionNames.toString());
//  }



  public List<ParameterizedAction> getParameterizedActions() {
    return parameterizedActions;
  }

  public List<DownloadContentAction> getDownloadContentActions() {
    return downloadContentActions;
  }

  public List<CreateUrlAction> getCreateUrlActions() {
    return openUrlActions;
  }

  public ParameterizedAction getParameterizedAction(String id) {
    StringBuffer actionNames = new StringBuffer();  
    for (ParameterizedAction action : getParameterizedActions()) {
      if (action.getId().equals(id)) {
        return action;
      } else {
         if (actionNames.length() > 0) {
           actionNames.append(", ");
         }
         actionNames.append(action.getId());
      }      
    }
  throw new RepositoryException("Action '" + id + "' not found, cannot be executed. Existing actions are: " + actionNames.toString());
  }

  public Long getRevision() {
    return this.revision;
  }
  
  public void setMimeType(MimeType mimeType) {
    this.mimeType = mimeType;
  }
  
  public void addDefaultContentRepresentation(ContentRepresentation contentRepresentation, ContentProvider provider) {
    addContentRepresentation(contentRepresentation, provider);
    defaultContentRepresentation = contentRepresentation;
  }

  public void addContentRepresentation(ContentRepresentation contentRepresentation, ContentProvider provider) {
    contentRepresentationList.add(contentRepresentation);
    contentProviderMap.put(contentRepresentation.getId(), provider);
  }

  public void addParameterizedAction(ParameterizedAction action) {
    parameterizedActions.add(action);
  }

  public void addOpenUrlAction(CreateUrlAction action) {
    openUrlActions.add(action);
  }

  public void addDownloadContentAction(String contentRepresentationId) {
    downloadContentActions.add(new DownloadContentActionImpl("Download " + contentRepresentationId, getContentRepresentation(contentRepresentationId)));
  }

  public List<RepositoryArtifactOpenLinkAction> createOpenLinkActions(RepositoryConnector connector, RepositoryArtifact artifact) {
    ArrayList<RepositoryArtifactOpenLinkAction> list = new ArrayList<RepositoryArtifactOpenLinkAction>();
    for (CreateUrlAction action : getCreateUrlActions()) {
      RepositoryArtifactOpenLinkAction link = new RepositoryArtifactOpenLinkAction(action.getId(), action.getUrl(connector, artifact));
      list.add(link);
    }
    return list;
  }
  
  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" + id + "]";
  }

  public void setRevision(Long revision) {
    this.revision = revision;
  }
  
}
