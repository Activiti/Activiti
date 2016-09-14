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
package org.activiti.app.rest.runtime;

import javax.servlet.http.HttpServletResponse;

import org.activiti.app.model.runtime.ProcessInstanceRepresentation;
import org.activiti.form.model.FormDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing a process instance.
 */
@RestController
public class ProcessInstanceResource extends AbstractProcessInstanceResource {

  @RequestMapping(value = "/rest/process-instances/{processInstanceId}", method = RequestMethod.GET, produces = "application/json")
  public ProcessInstanceRepresentation getProcessInstance(@PathVariable String processInstanceId, HttpServletResponse response) {
    return super.getProcessInstance(processInstanceId, response);
  }

  @RequestMapping(value = "/rest/process-instances/{processInstanceId}/start-form", method = RequestMethod.GET, produces = "application/json")
  public FormDefinition getProcessInstanceStartForm(@PathVariable String processInstanceId, HttpServletResponse response) {
    return super.getProcessInstanceStartForm(processInstanceId, response);
  }

  @RequestMapping(value = "/rest/process-instances/{processInstanceId}", method = RequestMethod.DELETE)
  @ResponseStatus(value = HttpStatus.OK)
  public void deleteProcessInstance(@PathVariable String processInstanceId) {
    super.deleteProcessInstance(processInstanceId);
  }

}
