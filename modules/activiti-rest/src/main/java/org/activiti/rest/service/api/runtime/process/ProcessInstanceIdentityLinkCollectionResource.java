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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.RestIdentityLink;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;


/**
 * @author Frederik Heremans
 */
public class ProcessInstanceIdentityLinkCollectionResource extends BaseProcessInstanceResource {

  @Get
  public List<RestIdentityLink> getIdentityLinks() {
    if(!authenticate())
      return null;
    
    List<RestIdentityLink> result = new ArrayList<RestIdentityLink>();
    ProcessInstance processInstance = getProcessInstanceFromRequest();
    
    List<IdentityLink> identityLinks = ActivitiUtil.getRuntimeService().getIdentityLinksForProcessInstance(processInstance.getId());
    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    for(IdentityLink link : identityLinks) {
      result.add(responseFactory.createRestIdentityLink(this, link));
    }
    return result;
  }
  
  @Post
  public RestIdentityLink createIdentityLink(RestIdentityLink identityLink) {
    if(!authenticate())
      return null;
    
    ProcessInstance processInstance = getProcessInstanceFromRequest();
    
    if(identityLink.getGroup() != null)  {
      throw new ActivitiIllegalArgumentException("Only user identity links are supported on a process instance.");
    }
    
    if(identityLink.getUser() == null)  {
      throw new ActivitiIllegalArgumentException("The user is required.");
    }
    
    if(identityLink.getType() == null) {
      throw new ActivitiIllegalArgumentException("The identity link type is required.");
    }

    ActivitiUtil.getRuntimeService().addUserIdentityLink(processInstance.getId(), identityLink.getUser(), identityLink.getType());
    
    setStatus(Status.SUCCESS_CREATED);
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createRestIdentityLink(this, identityLink.getType(), identityLink.getUser(), identityLink.getGroup(), null, null, processInstance.getId());
  }
}
