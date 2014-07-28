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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.resource.Get;


/**
 * @author Frederik Heremans
 */
public class ProcessInstanceIdentityLinkResource extends BaseProcessInstanceResource {

  @Get
  public RestIdentityLink getIdentityLink() {
    if(!authenticate())
      return null;
    
    ProcessInstance processInstance = getProcessInstanceFromRequest();
    
    // Extract and validate identity link from URL
    String identityId = getAttribute("identityId");
    String type = getAttribute("type");
    validateIdentityLinkArguments(identityId, type);
    
    IdentityLink link = getIdentityLink(identityId, type, processInstance.getId());
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createRestIdentityLink(this, link);
  }
  
  protected void validateIdentityLinkArguments(String identityId, String type) {
    if(identityId == null) {
      throw new ActivitiIllegalArgumentException("IdentityId is required.");
    }
    if(type == null) {
      throw new ActivitiIllegalArgumentException("Type is required.");
    }
  }
  
  protected IdentityLink getIdentityLink(String identityId, String type, String processInstanceId) {
    // Perhaps it would be better to offer getting a single identitylink from the API
    List<IdentityLink> allLinks = ActivitiUtil.getRuntimeService().getIdentityLinksForProcessInstance(processInstanceId);
    for(IdentityLink link : allLinks) {
      if(identityId.equals(link.getUserId()) && link.getType().equals(type)) {
        return link;
      }
    }
    throw new ActivitiObjectNotFoundException("Could not find the requested identity link.", IdentityLink.class);
  }
}
