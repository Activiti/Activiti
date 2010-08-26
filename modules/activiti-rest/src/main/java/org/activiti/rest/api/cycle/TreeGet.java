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
package org.activiti.rest.api.cycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNode;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

/**
 * @author Nils Preusker
 */
public class TreeGet extends ActivitiWebScript {

  private static Logger log = Logger.getLogger(TreeGet.class.getName());

  @Override
  protected void executeWebScript(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model) {

    String cuid = getCurrentUserId(req);

    HttpSession session = ((WebScriptServletRequest) req).getHttpServletRequest().getSession(true);
    RepositoryConnector conn = SessionUtil.getRepositoryConnector(cuid, session);

    String id = getString(req, "id");
    boolean folder = Boolean.parseBoolean(getString(req, "folder"));
    List<RepositoryNode> subtree = new ArrayList<RepositoryNode>();
    if (folder) {
      try {
        subtree = conn.getChildNodes(id);
      } catch (RepositoryException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
        // TODO: how can we let the user know what went wrong without breaking the tree?
        // throwing a HTTP 500 here will cause the tree to load the node for ever. 
        // throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
        // "exception.message");
      }
    }

    List<RepositoryArtifact> files = new ArrayList<RepositoryArtifact>();
    List<RepositoryFolder> folders = new ArrayList<RepositoryFolder>();

    for (RepositoryNode node : subtree) {
      if (node.getClass().isAssignableFrom(RepositoryArtifact.class)) {
        files.add((RepositoryArtifact) node);
      } else if (node.getClass().isAssignableFrom(RepositoryFolder.class)) {
        folders.add((RepositoryFolder) node);
      }
    }

    model.put("files", files);
    model.put("folders", folders);
  }
}
