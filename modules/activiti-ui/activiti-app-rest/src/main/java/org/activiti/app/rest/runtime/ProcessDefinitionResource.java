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

import javax.servlet.http.HttpServletRequest;

import org.activiti.form.model.FormDefinition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessDefinitionResource extends AbstractProcessDefinitionResource {
    
    @RequestMapping(value = "/rest/process-definitions/{processDefinitionId}/start-form", method = RequestMethod.GET, produces = "application/json")
    public FormDefinition getProcessDefinitionStartForm(HttpServletRequest request) {
    	return super.getProcessDefinitionStartForm(request);
    }
}