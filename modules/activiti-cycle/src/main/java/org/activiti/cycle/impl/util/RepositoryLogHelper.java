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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeCollection;

/**
 * 
 * @author bernd.ruecker@camunda.com
 */
public class RepositoryLogHelper {

  private static Logger log = Logger.getLogger(RepositoryLogHelper.class.getName());

  public static void logFolder(RepositoryConnector connector, RepositoryNodeCollection folder) {
    printFolder(connector, "", folder);
  }

  public static void printFolder(RepositoryConnector connector, RepositoryNodeCollection folder) {
    printFolder(connector, "", folder);
  }
  
  public static void printNodes(RepositoryConnector connector, List<RepositoryNode> nodes) {
    printNodes(connector, "", nodes);
  }
  
  public static void logFolder(RepositoryConnector connector, String intend, RepositoryNodeCollection folder) {
    log.log(Level.INFO, intend + folder);
    for (RepositoryFolder subFolder : folder.getFolderList()) {
      printFolder(connector, intend + "   ", connector.getChildren(subFolder.getNodeId()));
    }
    for (RepositoryArtifact file : folder.getArtifactList()) {
      log.log(Level.INFO, intend + "-" + file);
    }
  }  

  public static void printFolder(RepositoryConnector connector, String intend, RepositoryNodeCollection folder) {
    System.out.println(intend + "+" + folder);
    printNodes(connector, folder.asList());
  }
  
  public static void printNodes(RepositoryConnector connector, String intend, List<RepositoryNode> nodes) {    
    for (RepositoryNode node : nodes) {
      if (node instanceof RepositoryFolder) {
        printFolder(connector, intend + "   ", connector.getChildren(node.getNodeId()));
      } else {
        printArtifact(intend, (RepositoryArtifact) node);
      }
    }    
  }

  public static void printArtifact(RepositoryArtifact artifact) {
    printArtifact("", artifact);
  }
  
  public static void printArtifact(String intend, RepositoryArtifact artifact) {
    System.out.println(intend + "-" + artifact);
  }
}
