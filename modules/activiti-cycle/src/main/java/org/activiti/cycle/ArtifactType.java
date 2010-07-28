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

import org.activiti.cycle.impl.RepositoryRegistry;

/**
 * The type specifies the type of an artifact, e.g. Signavio model, jpdl process
 * model, text file, word document, ...
 * 
 * Linked to this type {@link ContentRepresentation}s for the GUI and
 * {@link ArtifactAction}s are defined.
 * 
 * TODO: Think about hierarchy
 * 
 * @author bernd.ruecker@camunda.com
 */
public class ArtifactType {

  /**
   * Human readable name of type
   */
  private String name;

  /**
   * the unique identifier for this file (e.g. file extension for files, namespace for Signavio, ...)
   */
  private String typeIdentifier;

  public ArtifactType() {
  }

  public ArtifactType(String name, String typeIdentifier) {
    this.name = name;
    this.typeIdentifier = typeIdentifier;
  }

  public List<Class< ? extends ArtifactAction>> getRegisteredActions() {
    return RepositoryRegistry.getArtifactAction(getTypeIdentifier());
  }

  public List<Class< ? extends ContentRepresentationProvider>> getContentRepresentationProviders() {
    return RepositoryRegistry.getContentRepresentationProviders(getTypeIdentifier());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTypeIdentifier() {
    return typeIdentifier;
  }

}
