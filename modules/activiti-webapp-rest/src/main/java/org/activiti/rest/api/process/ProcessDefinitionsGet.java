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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQueryProperty;
import org.activiti.rest.model.RestProcessDefinition;
import org.activiti.rest.util.ActivitiPagingWebScript;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * Returns details about the process definitions.
 * 
 * @author Erik Winlof
 */
public class ProcessDefinitionsGet extends ActivitiPagingWebScript {

  public ProcessDefinitionsGet() {
    properties.put("id", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
    properties.put("key", ProcessDefinitionQueryProperty.KEY);
    properties.put("version", ProcessDefinitionQueryProperty.VERSION);
    properties.put("deploymentId", ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
  }

  /**
   * Collects details about the process definitions for the webscript template.
   * 
   * @param req
   *          The webscripts request
   * @param status
   *          The webscripts status
   * @param cache
   *          The webscript cache
   * @param model
   *          The webscripts template model
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    paginateList(req, getRepositoryService().createProcessDefinitionQuery(), "processDefinitions", model, "id");

    // The paginated ProcessDefinitions should be wrapped in a
    // RestProcessDefinition
    List<ProcessDefinition> processDefinitions = (List<ProcessDefinition>) model.get("processDefinitions");
    if (processDefinitions != null) {
      List<RestProcessDefinition> processDefinitionTasks = new ArrayList<RestProcessDefinition>();
      for (ProcessDefinition p : processDefinitions) {
        RestProcessDefinition restProcessDefinition = new RestProcessDefinition((ProcessDefinitionEntity) p);
        StartFormData startFormData = getFormService().getStartFormData(p.getId());
        if (startFormData != null) {
          restProcessDefinition.setStartFormResourceKey(startFormData.getFormKey());
        }
        processDefinitionTasks.add(restProcessDefinition);
      }
      // Add the list of wrapped Tasks to the model
      model.put("processDefinitions", processDefinitionTasks);
    }
  }

}
