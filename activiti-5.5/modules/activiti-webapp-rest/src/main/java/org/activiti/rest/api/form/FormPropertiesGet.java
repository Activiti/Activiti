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

package org.activiti.rest.api.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.form.FormProperty;
import org.activiti.rest.model.RestFormProperty;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * Returns form properties of a running task.
 * @author Stefan Schröder
 */
public class FormPropertiesGet extends ActivitiWebScript {

  /**
   * Collects form properties of a task for the webscript template.
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
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    String taskId = req.getMandatoryPathParameter("taskId");
    List<FormProperty> properties = getFormService().getTaskFormData(taskId).getFormProperties();
    List<FormProperty> restProperties = new ArrayList<FormProperty>();
    for (FormProperty property : properties) {
      restProperties.add(new RestFormProperty(property));
    }
    model.put("formproperties", restProperties);
  }
}
