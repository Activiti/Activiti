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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Information about an artifact contained in the repository
 * (e.g. a file, signavio model, ...)
 * 
 * @author bernd.ruecker@camunda.com
 */
public class RepositoryArtifact extends RepositoryNode {

  private static final long serialVersionUID = 1L;

  // TODO: Think about file types, associated actions and so on. What impact
  // does it have on the content? ...?
  private ArtifactType artifactType;
  
  /**
   * list for {@link ContentRepresentationDefinition}s, which is lazily loaded when you
   * first query it
   */
  private List<ContentRepresentationDefinition> contentRepresentationDefinitionList;
  private Map<String, ContentRepresentationProvider> contentRepresentationProviderMap;
  
  private List<ArtifactAction> cachedFileActions;

  public RepositoryArtifact() {
  }

  public RepositoryArtifact(RepositoryConnector connector) {
    super(connector);
  }
  
  public String toString() {
    return this.getClass().getSimpleName() + " [id=" + getId() + ";type=" + artifactType + ";metadata=" + getMetadata() + "]";
  }  

  /**
   * load Content Representation with content as byte[] included for given
   * {@link RepositoryArtifact}
   */
  public Content loadContent(String representationName) {
    ContentRepresentationProvider provider = getContentRepresentationProviderMap().get(representationName);
    if (provider == null) {
      throw new RepositoryException("Couldn't find or load content representation '" + representationName + "' for artifact " + this);
    }
    return provider.createContent(this);
  }

  public ContentRepresentationDefinition getContentRepresentationDefinition(String contentRepresentationName) {
    for (ContentRepresentationDefinition def : getContentRepresentationDefinitions()) {
      if (def.getName().equals(contentRepresentationName)) {
        return def;
      }
    }
    throw new RepositoryException("Couldn't find ContentRepresentationDefinition with name '" + contentRepresentationName + "'");
  }
  
  public Collection<ContentRepresentationDefinition> getContentRepresentationDefinitions() {
    if (contentRepresentationDefinitionList == null) {    
    // if not done already lazy load the content from the registered providers
    contentRepresentationDefinitionList = new ArrayList<ContentRepresentationDefinition>();
      for (ContentRepresentationProvider provider : getContentRepresentationProviders()) {
        ContentRepresentationDefinition cr = provider.createContentRepresentationDefinition(this);
        if (cr != null) {
          contentRepresentationDefinitionList.add(cr);
        } else {
          log.warning("content provider '" + provider + "' created NULL instead of proper ContentRepresentation object for artifact " + this
                  + ". Check configuration!");
        }
      }
    }    
    return contentRepresentationDefinitionList;
  }  

  public Collection<ContentRepresentationProvider> getContentRepresentationProviders() {
    return getContentRepresentationProviderMap().values();
  }

  public Map<String, ContentRepresentationProvider> getContentRepresentationProviderMap() {
    if (contentRepresentationProviderMap == null) {
      contentRepresentationProviderMap = new HashMap<String, ContentRepresentationProvider>();
      if (getArtifactType() != null) {
        for (Class< ? extends ContentRepresentationProvider> providerClass : getArtifactType().getContentRepresentationProviders()) {
          try {
            ContentRepresentationProvider p = providerClass.newInstance();
            contentRepresentationProviderMap.put(p.getContentRepresentationName(), p);
          } catch (Exception ex) {
            log.log(Level.SEVERE, "couldn't create content provider of class " + providerClass, ex);
          }
        }
      }
    }
    return contentRepresentationProviderMap;
  }

  public void initializeActions() {
    cachedFileActions = new ArrayList<ArtifactAction>();
    if (getArtifactType() != null) {
      cachedFileActions = new ArrayList<ArtifactAction>();
      
      for (Class< ? extends ArtifactAction> clazz : artifactType.getRegisteredActions()) {
        try {
          ArtifactAction action = clazz.newInstance();
          action.setArtifact(this);
          cachedFileActions.add(action);
        } catch (Exception ex) {
          log.log(Level.SEVERE, "couldn't create file action of class " + clazz, ex);
        }
      }
      
      cachedFileActions.addAll(createDownloadContentActions());
      
      log.fine("Actions for artifact '" + getId() + "' with type " + getArtifactType().getName() + " requested, returning " + cachedFileActions.size()
              + " actions.");
    } else {
      log.fine("No artifact type set for artifact '" + getId() + "'. Don't return any actions.");      
    }
    
  }

  private List<ArtifactAction> getRegisteredActions() {
    if (cachedFileActions == null) {
      initializeActions();
    }

    return cachedFileActions;
  }
  
  public List<DownloadContentAction> createDownloadContentActions() {
    // TODO: Think about a better handling of initialization of providers and representation definitions
    // For the moment just query the list beforehand to make sure it is initialized
    getContentRepresentationDefinitions();
    
    ArrayList<DownloadContentAction> actions = new ArrayList<DownloadContentAction>();

    for (ContentRepresentationProvider provider : getContentRepresentationProviders()) {
      if (provider.isContentDownloadable()) {
        actions.add(new DownloadContentAction(this, provider.getContentRepresentationName()));
      }
    }

    return actions;
  }
  
  public List<ParametrizedAction> getParametrizedActions() {
    ArrayList<ParametrizedAction> actions = new ArrayList<ParametrizedAction>();
    for (ArtifactAction action : getRegisteredActions()) {
      if (action instanceof ParametrizedAction) {
        actions.add((ParametrizedAction) action);
      }
    }
    return actions;
  }

  public List<OpenUrlAction> getOpenUrlActions() {
    ArrayList<OpenUrlAction> actions = new ArrayList<OpenUrlAction>();
    for (ArtifactAction action : getRegisteredActions()) {
      if (action instanceof OpenUrlAction) {
        actions.add((OpenUrlAction) action);
      }
    }
    return actions;   
  }
  

  public List<DownloadContentAction> getDownloadContentActions() {
    ArrayList<DownloadContentAction> actions = new ArrayList<DownloadContentAction>();
    for (ArtifactAction action : getRegisteredActions()) {
      if (action instanceof DownloadContentAction) {
        actions.add((DownloadContentAction) action);
      }
    }
    return actions;
  }  
  
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

  
  /**
   * execute the action with the given name and the given parameters.
   * 
   * Only {@link ParametrizedAction}s can be executed with parameters, if you
   * try to execute another action type, a {@link RepositoryException} class is
   * thrown
   */
  public void executeAction(String name, Map<String, Object> parameters) throws Exception {
    StringBuffer actionNames = new StringBuffer();
    for (ArtifactAction action : getRegisteredActions()) {
      if (action.getName().equals(name)) {
        if (action instanceof ParametrizedAction) {
          ((ParametrizedAction) action).execute(parameters);
          return;
        } else {
          throw new RepositoryException("cannot execute action '" + name + "' with parameters, because it is not a ParametrizedAction");
        }
      }
      else {
        if (actionNames.length() > 0) {
          actionNames.append(", ");
        }
        actionNames.append(action.getName());
      }
    }
    throw new RepositoryException("Action '" + name + "' not found, cannot be executed. Existing actions are: " + actionNames.toString());
  }

  public ArtifactType getArtifactType() {
    return artifactType;
  }

  public void setArtifactType(ArtifactType fileType) {
    this.artifactType = fileType;
  }

}
