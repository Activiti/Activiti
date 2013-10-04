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

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.common.api.ActivitiUtil;
import org.restlet.resource.Get;

/**
 * @author Frederik Heremans
 */
public class ProcessDefinitionModelResource extends BaseProcessDefinitionResource {

  @Get
  public BpmnModel getModelResource() {
    if (authenticate() == false)
      return null;
    ProcessDefinition processDefinition = getProcessDefinitionFromRequest();
    return ActivitiUtil.getRepositoryService().getBpmnModel(processDefinition.getId());
  }
  
}
