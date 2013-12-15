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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;

/**
 * @author Frederik Heremans
 */
public class BaseModelResource extends SecuredResource {

  /**
   * Returns the {@link Model} that is requested. Throws the right exceptions
   * when bad request was made or model is not found.
   */
  protected Model getModelFromRequest() {
    String modelId = getAttribute("modelId");
    if(modelId == null) {
      throw new ActivitiIllegalArgumentException("The modelId cannot be null");
    }
    
    Model model = ActivitiUtil.getRepositoryService().createModelQuery()
            .modelId(modelId).singleResult();
   
   if(model == null) {
     throw new ActivitiObjectNotFoundException("Could not find a model with id '" + modelId + "'.", ProcessDefinition.class);
   }
   return model;
  }
}
