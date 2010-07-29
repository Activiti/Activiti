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
  private List<ArtifactAction> cachedFileActions;

  public RepositoryArtifact() {
  }

  public RepositoryArtifact(RepositoryConnector connector) {
    super(connector);
  }
  
  /**
   * load Content Representation with content as byte[] included for given
   * {@link RepositoryArtifact}
   */
  public static ContentRepresentation getContentRepresentation(RepositoryArtifact artifact, String representationName) {
    if (artifact.getArtifactType() != null) {
      for (Class< ? extends ContentRepresentationProvider> providerClass : artifact.getArtifactType().getContentRepresentationProviders()) {
        try {
          ContentRepresentationProvider p = providerClass.newInstance();
          if (p.getContentRepresentationName().equals(representationName)) {
            return p.createContentRepresentation(artifact, true);
          }
        } catch (Exception ex) {
          log.log(Level.SEVERE, "couldn't create content provider of class " + providerClass, ex);
        }
      }
    }
    throw new RepositoryException("Couldn't find or load content representation '" + representationName + "' for artifact " + artifact);
  }

  public List<ContentRepresentation> getContentRepresentations() {
    if (contentRepresentationList != null) {
      return contentRepresentationList;
    }
    
    // if not done already lazy load the content from the registered providers
    contentRepresentationList = new ArrayList<ContentRepresentation>();
    if (getArtifactType() != null) {
      for (Class< ? extends ContentRepresentationProvider> providerClass : getArtifactType().getContentRepresentationProviders()) {
        try {
          ContentRepresentationProvider p = providerClass.newInstance();
          ContentRepresentation cr = p.createContentRepresentation(this, false);
          if (cr != null) {
            contentRepresentationList.add(cr);
          } else {
            log.warning("content provider '" + p + "' created NULL instead of proper ContentRepresentation object for artifact " + this
                    + ". Check configuration!");
          }
        }
        catch (Exception ex) {
          log.log(Level.SEVERE, "couldn't create content provider of class " + providerClass, ex);
        }
      }
    }
    
    return contentRepresentationList;
  }  

  public List<ArtifactAction> getActions() {
    if (cachedFileActions != null) {
      return cachedFileActions;
    }
    
    cachedFileActions = new ArrayList<ArtifactAction>();
    if (getArtifactType() != null) {
      cachedFileActions = new ArrayList<ArtifactAction>();
      
      for (Class< ? extends ArtifactAction> clazz : artifactType.getRegisteredActions()) {
        try {
          ArtifactAction action = clazz.newInstance();
          action.setFile(this);
          cachedFileActions.add(action);
        } catch (Exception ex) {
          log.log(Level.SEVERE, "couldn't create file action of class " + clazz, ex);
        }
      }
    }

    log.fine("Actions for file type " + getArtifactType().getName() + " requested, returning " + cachedFileActions.size() + " actions.");

    return cachedFileActions;
  }

  public ArtifactType getArtifactType() {
    return artifactType;
  }

  public void setArtifactType(ArtifactType fileType) {
    this.artifactType = fileType;
  }

}
