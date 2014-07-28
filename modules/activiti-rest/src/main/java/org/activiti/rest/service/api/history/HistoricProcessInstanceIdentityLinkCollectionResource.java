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

package org.activiti.rest.service.api.history;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.resource.Get;


/**
 * @author Tijs Rademakers
 */
public class HistoricProcessInstanceIdentityLinkCollectionResource extends SecuredResource {

  @Get
  public List<HistoricIdentityLinkResponse> getTaskIdentityLinks() {
    if(!authenticate()) {
      return null;
    }
    
    String processInstanceId = getAttribute("processInstanceId");
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("The processInstanceId cannot be null");
    }
    
    List<HistoricIdentityLink> identityLinks = ActivitiUtil.getHistoryService().getHistoricIdentityLinksForProcessInstance(processInstanceId);
    
    List<HistoricIdentityLinkResponse> responseList = new ArrayList<HistoricIdentityLinkResponse>();
    if (identityLinks != null) {
      RestResponseFactory restResponseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
      for (HistoricIdentityLink instance : identityLinks) {
        responseList.add(restResponseFactory.createHistoricIdentityLinkResponse(this, instance));
      }
    }
    
    return responseList;
  }
}
