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
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class ProcessInstanceIdentityLinkResource extends BaseProcessInstanceResource {

  @RequestMapping(value="/runtime/process-instances/{processInstanceId}/identitylinks/users/{identityId}/{type}", method = RequestMethod.GET, produces="application/json")
  public RestIdentityLink getIdentityLink(@PathVariable("processInstanceId") String processInstanceId, 
      @PathVariable("identityId") String identityId, @PathVariable("type") String type, HttpServletRequest request) {
    
    ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);
    
    validateIdentityLinkArguments(identityId, type);
    
    IdentityLink link = getIdentityLink(identityId, type, processInstance.getId());
    return restResponseFactory.createRestIdentityLink(link);
  }
  
  @RequestMapping(value="/runtime/process-instances/{processInstanceId}/identitylinks/users/{identityId}/{type}", method = RequestMethod.DELETE)
  public void deleteIdentityLink(@PathVariable("processInstanceId") String processInstanceId, 
      @PathVariable("identityId") String identityId, @PathVariable("type") String type, HttpServletResponse response) {
    
    ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);
    
    validateIdentityLinkArguments(identityId, type);
    
    getIdentityLink(identityId, type, processInstance.getId());
    
    runtimeService.deleteUserIdentityLink(processInstance.getId(), identityId, type);
    
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
  
  protected void validateIdentityLinkArguments(String identityId, String type) {
    if (identityId == null) {
      throw new ActivitiIllegalArgumentException("IdentityId is required.");
    }
    if (type == null) {
      throw new ActivitiIllegalArgumentException("Type is required.");
    }
  }
  
  protected IdentityLink getIdentityLink(String identityId, String type, String processInstanceId) {
    // Perhaps it would be better to offer getting a single identity link from the API
    List<IdentityLink> allLinks = runtimeService.getIdentityLinksForProcessInstance(processInstanceId);
    for (IdentityLink link : allLinks) {
      if (identityId.equals(link.getUserId()) && link.getType().equals(type)) {
        return link;
      }
    }
    throw new ActivitiObjectNotFoundException("Could not find the requested identity link.", IdentityLink.class);
  }
}
