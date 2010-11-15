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

import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiRequestObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * Creates a new Tag that is associated to the artifact (identified by
 * artifactId and connectorId).
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class TagPost extends ActivitiCycleWebScript {

  /**
   * Creates a new Tag that is associated to the artifact (identified by
   * artifactId and connectorId).
   * 
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    ActivitiRequestObject obj = req.getBody();

    String connectorId = req.getMandatoryString(obj, "connectorId");
    String artifactId = req.getMandatoryString(obj, "artifactId");
    String tagName = req.getMandatoryString(obj, "tagName");
    String alias = req.getMandatoryString(obj, "alias");

    this.cycleService.addTag(connectorId, artifactId, tagName, alias);
    model.put("connectorId", connectorId);
    model.put("artifactId", artifactId);
    model.put("tagName", tagName);
    model.put("alias", alias);
  }
}
