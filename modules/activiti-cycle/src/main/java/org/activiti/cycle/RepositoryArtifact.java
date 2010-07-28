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
   * list for {@link ContentRepresentation}s, which is lazily loaded when you
   * first query it
   */
  private List<ContentRepresentation> contentRepresentationList;

  private transient List<ArtifactAction> cachedFileActions;
  private transient ArtifactAction cachedDefaultFileAction;

  public RepositoryArtifact() {
  }

  public RepositoryArtifact(RepositoryConnector connector) {
    super(connector);
  }

  public List<ContentRepresentation> getContentRepresentations() {
    if (contentRepresentationList != null) {
      return contentRepresentationList;
    }
    
    // if not done already lazy load the content from the registered providers
    contentRepresentationList = new ArrayList<ContentRepresentation>();
    if (getArtifactType() != null) {
      List<ContentRepresentationProvider> providers = getArtifactType().getContentRepresentationProviders();
      for (ContentRepresentationProvider p : providers) {
        ContentRepresentation cr = p.createContentRepresentation(this, false);
        if (cr!=null) {
          cr.setArtifact(this);
          contentRepresentationList.add(cr);
        }
        else {
          log.warning("content provider '" + p + "' created NULL instead of proper ContentRepresentation object for artifact " + this
                  + ". Check configuration!");
        }
      }
    }
    
    return contentRepresentationList;
  }  

  public List<ArtifactAction> getActions() {
    if (getArtifactType() == null) {
      return new ArrayList<ArtifactAction>();
    }

    if (cachedFileActions == null) {
      cachedFileActions = new ArrayList<ArtifactAction>();
      for (Class< ? extends ArtifactAction> clazz : getRegisteredActionTypes()) {
        try {
          ArtifactAction action = clazz.newInstance();
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

    log.fine("Actions for file type " + getArtifactType().getName() + " requested, returning " + cachedFileActions.size() + " actions.");

    return cachedFileActions;
  }

  @Deprecated
  public ArtifactAction getDefaultFileAction() {
    if (cachedDefaultFileAction == null) {
      // lazy loading of action definitions if not already done
      getActions();
    }

    log.info("Default actions for file type " + getArtifactType() + " requested, returning "
            + (cachedDefaultFileAction == null ? "null" : cachedDefaultFileAction.getName()));

    return cachedDefaultFileAction;
  }

  @Deprecated
  public boolean isDefaultAction(Class< ? extends ArtifactAction> actionType) {
    if (artifactType != null && artifactType.getDefaultAction() != null) {
      return artifactType.getDefaultAction().equals(actionType);
    } else {
      return false;
    }
  }

  public List<Class< ? extends ArtifactAction>> getRegisteredActionTypes() {
    if (artifactType != null) {
      return artifactType.getRegisteredActions();
    } else {
      return new ArrayList<Class< ? extends ArtifactAction>>();
    }
  }

  public ArtifactType getArtifactType() {
    return artifactType;
  }

  public void setArtifactType(ArtifactType fileType) {
    this.artifactType = fileType;
  }

}
