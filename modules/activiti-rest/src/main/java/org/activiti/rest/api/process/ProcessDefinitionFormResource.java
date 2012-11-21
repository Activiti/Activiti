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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class ProcessDefinitionFormResource extends SecuredResource {
  
  @Get
  public InputRepresentation getProcessDefinitions() {
    if(authenticate() == false) return null;
    
    String processDefinitionId = (String) getRequest().getAttributes().get("processDefinitionId");
    Object form = ActivitiUtil.getFormService().getRenderedStartForm(processDefinitionId);
    InputStream is = null;
    if (form != null && form instanceof String) {
      is = new ByteArrayInputStream(((String) form).getBytes());
    }
    else if (form != null && form instanceof InputStream) {
      is = (InputStream) form;
    }
    if (is != null) {
      InputRepresentation output = new InputRepresentation(is);
      return output;
    
    } else if (form != null){
      throw new ActivitiException("The form for process definition '" + processDefinitionId + "' failed to render.");
    
    } else {
      throw new ActivitiException("The form for process definition '" + processDefinitionId + "' cannot be rendered using the rest api.");
    }
  }
}
