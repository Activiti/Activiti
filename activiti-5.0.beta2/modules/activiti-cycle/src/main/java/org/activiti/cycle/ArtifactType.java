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

import java.util.List;

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
public interface ArtifactType {

  public String getId();

  /**
   * list of {@link ContentRepresentation} in the configured order
   */
  public List<ContentRepresentation> getContentRepresentations();

  // public ContentRepresentation getContentRepresentation(String id);

  public ContentRepresentation getDefaultContentRepresentation();  

  public ContentProvider getContentProvider(String contentRepresentationId);

  public List<ParameterizedAction> getParameterizedActions();
  
  public ParameterizedAction getParameterizedAction(String name);

  public List<RepositoryArtifactOutgoingLink> createLinks(RepositoryConnector connector, RepositoryArtifact artifact);

  public List<DownloadContentAction> getDownloadContentActions();
  
}
