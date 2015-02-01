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

package org.activiti.rest.service.api.repository;

import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tijs Rademakers
 */
@RestController
public class ProcessDefinitionImageResource extends BaseProcessDefinitionResource {

  @RequestMapping(value="/repository/process-definitions/{processDefinitionId}/image", method = RequestMethod.GET, produces="image/png")
  public @ResponseBody byte[] getModelResource(@PathVariable String processDefinitionId) {
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest(processDefinitionId);
    InputStream imageStream = repositoryService.getProcessDiagram(processDefinition.getId());
    try {
      return IOUtils.toByteArray(imageStream);
    } catch(Exception e) {
      throw new ActivitiException("Error reading image stream", e);
    }
  }
  
}
