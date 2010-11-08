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

import java.util.Map;

import org.activiti.cycle.CycleService;
import org.activiti.cycle.RepositoryFolder;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiRequestObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * Creates a new {@link RepositoryFolder} instance and persists it using the
 * {@link CycleService}.
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class FolderPost extends ActivitiCycleWebScript {

  @Override
  void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    ActivitiRequestObject obj = req.getBody();

    String connectorId = req.getMandatoryString(obj, "connectorId");
    String parentFolderId = req.getMandatoryString(obj, "parentFolderId");
    String name = req.getMandatoryString(obj, "name");

    try {
      this.cycleService.createFolder(connectorId, parentFolderId, name);
      model.put("result", true);
    } catch (Exception e) {
      model.put("result", false);
      throw new RuntimeException(e);
    }
  }
}
