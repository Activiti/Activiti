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

package org.activiti.cycle.impl.connector.fs;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.SubProcessExpansion;

/**
 * Connector for testing {@link SubProcessExpansion}
 * 
 * Takes a Signavio model id and obtains an according JSON file from the file
 * system.
 * 
 * It needs to reside in 'src/main/java', because it is used in test cases of
 * the jBPM3 Plugin for Cycle. 
 *
 * @author Falko Menge <falko.menge@camunda.com>
 */
public class SignavioFileSystemConnector extends FileSystemConnector {

  /**
   * Takes a Signavio model id and obtains an according JSON file from the file
   * system.
   */
  public Content getContent(String artifactId) throws RepositoryNodeNotFoundException {
    if (artifactId.endsWith(".signavio.xml")) {
      artifactId = (String) artifactId.subSequence(15, artifactId.lastIndexOf(".signavio.xml"));
    }
    artifactId = "/" + artifactId + ".json";
    return super.getContent(artifactId);
  }
}