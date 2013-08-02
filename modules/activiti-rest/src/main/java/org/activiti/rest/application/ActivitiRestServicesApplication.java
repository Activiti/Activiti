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

package org.activiti.rest.application;

import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.List;
import org.activiti.rest.api.DefaultResource;
import org.activiti.rest.api.RestResponseFactory;
import org.activiti.rest.application.jackson.ISO8601JacksonConverter;
import org.activiti.rest.filter.JsonpFilter;
import org.restlet.Restlet;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.jackson.JacksonConverter;
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

    // Replace standard JacksonConverter with ISO8601JacksonConverter to enable ISO-date format by default
    List<ConverterHelper> registeredConverters = Engine.getInstance().getRegisteredConverters();
    int index = -1;
    for(int i = 0; i < registeredConverters.size(); i++) {
      if(registeredConverters.get(i) instanceof JacksonConverter) {
          index = i;
      }
    }
    if (index != -1) {
        registeredConverters.set(index, new ISO8601JacksonConverter());
    }
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
