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

package org.activiti.rest.service.api.runtime.process;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class ProcessInstanceIdentityLinkCollectionResource extends BaseProcessInstanceResource {

  @RequestMapping(value="/runtime/process-instances/{processInstanceId}/identitylinks", method = RequestMethod.GET, produces="application/json")
  public List<RestIdentityLink> getIdentityLinks(@PathVariable String processInstanceId, HttpServletRequest request) {
    ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);
    return restResponseFactory.createRestIdentityLinks(runtimeService.getIdentityLinksForProcessInstance(processInstance.getId()));
  }
  
  @RequestMapping(value="/runtime/process-instances/{processInstanceId}/identitylinks", method = RequestMethod.POST, produces="application/json")
  public RestIdentityLink createIdentityLink(@PathVariable String processInstanceId, @RequestBody RestIdentityLink identityLink,
      HttpServletRequest request, HttpServletResponse response) {
    
    ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);
    
    if (identityLink.getGroup() != null)  {
      throw new ActivitiIllegalArgumentException("Only user identity links are supported on a process instance.");
    }
    
    if (identityLink.getUser() == null)  {
      throw new ActivitiIllegalArgumentException("The user is required.");
    }
    
    if (identityLink.getType() == null) {
      throw new ActivitiIllegalArgumentException("The identity link type is required.");
    }

    runtimeService.addUserIdentityLink(processInstance.getId(), identityLink.getUser(), identityLink.getType());
    
    response.setStatus(HttpStatus.CREATED.value());
    
    return restResponseFactory.createRestIdentityLink(identityLink.getType(), identityLink.getUser(), 
        identityLink.getGroup(), null, null, processInstance.getId());
  }
}
