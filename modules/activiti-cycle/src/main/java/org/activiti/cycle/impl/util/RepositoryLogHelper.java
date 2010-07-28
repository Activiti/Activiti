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
package org.activiti.cycle.impl.util;

import java.util.logging.Logger;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryFolder;

/**
 * 
 * @author bernd.ruecker@camunda.com
 */
public class RepositoryLogHelper {

  private static Logger log = Logger.getLogger(RepositoryLogHelper.class.getName());
  
  public static void printFolder(RepositoryFolder folder) {
    printFolder("", folder);
  }

  public static void printFolder(String intend, RepositoryFolder folder) {
    log.fine(intend + folder);
    for (RepositoryFolder subFolder : folder.getSubFolders()) {
      printFolder(intend + "   ", subFolder);
    }
    for (RepositoryArtifact file : folder.getArtifacts()) {
      log.fine(intend + "-" + file);
    }
  }
}
