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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.RepositoryNodeCollection;
import org.activiti.cycle.impl.db.CycleServiceDbXStreamImpl;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * @author Nils Preusker
 */
public class TreeGet extends ActivitiWebScript {

  private static Logger log = Logger.getLogger(TreeGet.class.getName());

  // private CycleService cycleService;
  private RepositoryConnector repositoryConnector;

  private void init(ActivitiRequest req) {
    String cuid = req.getCurrentUserId();

    HttpSession session = req.getHttpServletRequest().getSession(true);
    // this.cycleService = SessionUtil.getCycleService();
    this.repositoryConnector = CycleServiceDbXStreamImpl.getRepositoryConnector(cuid, session);
  }
  
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    init(req);

    String id = req.getString("id");
    boolean folder = Boolean.parseBoolean(req.getString("folder"));
    if (folder) {
      try {
        RepositoryNodeCollection children = this.repositoryConnector.getChildren(id);

        model.put("files", children.getArtifactList());
        model.put("folders", children.getFolderList());
        return;

      } catch (RepositoryException e) {
        log.log(Level.SEVERE, "Cannot load children for id '" + id + "'", e);
        // TODO: how can we let the user know what went wrong without breaking the tree?
        // throwing a HTTP 500 here will cause the tree to load the node for ever. 
        // throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
        // "exception.message");

      }
    }

    // provide empty list as default
    model.put("files", new ArrayList<RepositoryArtifact>());
    model.put("folders", new ArrayList<RepositoryFolder>());
  }
}
