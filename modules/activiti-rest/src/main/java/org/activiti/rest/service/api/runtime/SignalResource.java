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

package org.activiti.rest.service.api.runtime;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.runtime.process.SignalEventReceivedRequest;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Post;

/**
 * Resource for notifying the engine a signal event has been received, independent of an execution.
 * 
 * @author Frederik Heremans
 */
public class SignalResource extends SecuredResource {


  @Post
  public void signalEventReceived(SignalEventReceivedRequest signalRequest) {
    if (authenticate() == false)
      return;
    
    if(signalRequest.getSignalName() == null) {
    	throw new ActivitiIllegalArgumentException("signalName is required");
    }
    
    Map<String, Object> signalVariables = null;
    if(signalRequest.getVariables() != null) {
    	RestResponseFactory factory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
      signalVariables = new HashMap<String, Object>();
      for(RestVariable variable : signalRequest.getVariables()) {
        if(variable.getName() == null) {
          throw new ActivitiIllegalArgumentException("Variable name is required.");
        }
        signalVariables.put(variable.getName(), factory.getVariableValue(variable));
      }
    }
    
    
    if(signalRequest.isAsync()) {
    	if(signalVariables != null) {
    		throw new ActivitiIllegalArgumentException("Async signals cannot take variables as payload");
    	}
    	
    	if(signalRequest.isCustomTenantSet()) {
    		ActivitiUtil.getRuntimeService().signalEventReceivedAsyncWithTenantId(signalRequest.getSignalName(), signalRequest.getTenantId());
    	} else {
    		ActivitiUtil.getRuntimeService().signalEventReceivedAsync(signalRequest.getSignalName());
    	}
    	setStatus(Status.SUCCESS_ACCEPTED);
    } else {
    	if(signalRequest.isCustomTenantSet()) {
    		ActivitiUtil.getRuntimeService().signalEventReceivedWithTenantId(signalRequest.getSignalName(), signalVariables, signalRequest.getTenantId());
    	} else {
    		ActivitiUtil.getRuntimeService().signalEventReceived(signalRequest.getSignalName(), signalVariables);
    	}
    	setStatus(Status.SUCCESS_OK);
    }
  }
}
