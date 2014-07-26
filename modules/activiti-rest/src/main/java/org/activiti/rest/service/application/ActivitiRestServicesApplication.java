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

package org.activiti.rest.service.application;

import org.activiti.rest.common.api.DefaultResource;
import org.activiti.rest.common.application.ActivitiRestApplication;
import org.activiti.rest.common.filter.JsonpFilter;
import org.activiti.rest.service.api.RestResponseFactory;
import org.restlet.Restlet;
import org.restlet.routing.Router;
/**
 * @author Tijs Rademakers
 */
public class ActivitiRestServicesApplication extends ActivitiRestApplication {

  protected RestResponseFactory restResponseFactory;
  
  public ActivitiRestServicesApplication() {
    super();
  }
  
  /**
   * Creates a root Restlet that will receive all incoming calls.
   */
  @Override
  public synchronized Restlet createInboundRoot() {
    initializeAuthentication();
    
    Router router = new Router(getContext());
    router.attachDefault(DefaultResource.class);
    RestServicesInit.attachResources(router);
    
    JsonpFilter jsonpFilter = new JsonpFilter(getContext());
    authenticator.setNext(jsonpFilter);
    jsonpFilter.setNext(router);

    return authenticator;
  }
  
  
  public void setRestResponseFactory(RestResponseFactory restResponseFactory) {
    this.restResponseFactory = restResponseFactory;
  }
  
  public RestResponseFactory getRestResponseFactory() {
    if(restResponseFactory == null) {
      restResponseFactory = new RestResponseFactory();
    }
    return restResponseFactory;
  }
}
