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

import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nils Preusker
 */
public class ArtifactGet extends ActivitiWebScript {

  @Override
  protected void executeWebScript(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model) {
    String artifactId = getString(req, "artifactId");
    // TODO: add service to retrieve artifacts by id

    long id = Long.parseLong(artifactId);

    model.put("artifact", new Artifact(id, id == 1 ? "http://jorambarrez.be/files/blog/bpmn2_sneakpeek/vacationRequest.png"
            : "http://www.jorambarrez.be/files/blog/jbpm_43_released/jbpm_evolution.png"));
  }
}
