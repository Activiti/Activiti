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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.CycleTagContent;
import org.activiti.cycle.RepositoryNodeTag;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class TagsGet extends ActivitiCycleWebScript {

  @Override
  void execute(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {

    String connectorId = req.getString("connectorId");
    String artifactId = req.getString("artifactId");
    String tag = req.getString("tag");

    Map<String, String> tags;
    if (connectorId != null && artifactId != null) {
      tags = getTagsByArtifact(connectorId, artifactId);
    } else if (tag != null) {
      tags = getTagsById(tag);
    } else {
      throw new WebScriptException(
              Status.STATUS_BAD_REQUEST,
              "Missing parameter, please provide either 'connectorId' and 'artifactId' to retrieve tags for an artifact or 'tag' to search for tags that contain the given string");
    }
    model.put("tags", tags);
  }

  /**
   * TODO: this method (or an equivalent with a more generic return type) should be moved to the API (CycleService)
   */
  private Map<String, String> getTagsById(String id) {
    List<CycleTagContent> allTags = this.cycleService.getRootTags();
    Map<String, String> tags = new HashMap<String, String>();
    for (CycleTagContent tag : allTags) {
      if (tag.getName().contains(id)) {
        tags.put(tag.getName(), tag.getName());
      }
    }
    return tags;
  }
  
  /**
   * TODO: this method (or an equivalent with a more generic return type) should be moved to the API (CycleService)
   */
  private Map<String, String> getTagsByArtifact(String connectorId, String artifactId) {
    Map<String, String> tags = new HashMap<String, String>();
    for(RepositoryNodeTag repositoryNodeTag : this.cycleService.getTagsForNode(connectorId, artifactId)) {
      tags.put(repositoryNodeTag.getName(), repositoryNodeTag.getAlias());
    }
    return tags;
  }

}
