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

import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.runtime.process.SignalEventReceivedRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource for notifying the engine a signal event has been received, independent of an execution.
 * 
 * @author Frederik Heremans
 */
@RestController
public class SignalResource {
  
  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected RuntimeService runtimeService;

  @RequestMapping(value="/runtime/signals", method = RequestMethod.POST)
  public void signalEventReceived(@RequestBody SignalEventReceivedRequest signalRequest, HttpServletResponse response) {
    if (signalRequest.getSignalName() == null) {
    	throw new ActivitiIllegalArgumentException("signalName is required");
    }
    
    Map<String, Object> signalVariables = null;
    if (signalRequest.getVariables() != null) {
      signalVariables = new HashMap<String, Object>();
      for (RestVariable variable : signalRequest.getVariables()) {
        if (variable.getName() == null) {
          throw new ActivitiIllegalArgumentException("Variable name is required.");
        }
        signalVariables.put(variable.getName(), restResponseFactory.getVariableValue(variable));
      }
    }
    
    if (signalRequest.isAsync()) {
    	if(signalVariables != null) {
    		throw new ActivitiIllegalArgumentException("Async signals cannot take variables as payload");
    	}
    	
    	if (signalRequest.isCustomTenantSet()) {
    	  runtimeService.signalEventReceivedAsyncWithTenantId(signalRequest.getSignalName(), signalRequest.getTenantId());
    	} else {
    	  runtimeService.signalEventReceivedAsync(signalRequest.getSignalName());
    	}
    	response.setStatus(HttpStatus.ACCEPTED.value());
    } else {
    	if (signalRequest.isCustomTenantSet()) {
    		runtimeService.signalEventReceivedWithTenantId(signalRequest.getSignalName(), signalVariables, signalRequest.getTenantId());
    	} else {
    		runtimeService.signalEventReceived(signalRequest.getSignalName(), signalVariables);
    	}
    	response.setStatus(HttpStatus.NO_CONTENT.value());
    }
  }
}
