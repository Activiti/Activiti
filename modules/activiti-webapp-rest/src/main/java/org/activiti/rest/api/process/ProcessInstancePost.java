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
package org.activiti.rest.api.process;

import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Map;

/**
 * Creates a new process instance base on a process definition id and returns details about the new process instance.
 *
 * @author Erik Winlof
 */
public class ProcessInstancePost extends ActivitiWebScript
{

  /**
   * Creates a new process instance base on a process definition id and collects details about the new
   * process instance for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model)
  {
    ActivitiRequest.ActivitiWebScriptBody body = req.getBody();
    String processDefinitionId = req.getMandatoryString(body, "processDefinitionId");
    Map<String, Object> variables = req.getFormVariables(body);
    variables.remove("processDefinitionId");
    model.put("instance", getRuntimeService().startProcessInstanceById(processDefinitionId, variables));
  }

}
