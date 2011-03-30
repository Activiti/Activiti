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

/**
 * Information about an artifact contained in the repository (e.g. a file,
 * signavio model, ...)
 * 
 * @author bernd.ruecker@camunda.com
 */
public interface RepositoryArtifact extends RepositoryNode {

  /**
   * @return the {@link RepositoryArtifactType} of this
   *         {@link RepositoryArtifact}.
   * 
   */
  public RepositoryArtifactType getArtifactType();

}
