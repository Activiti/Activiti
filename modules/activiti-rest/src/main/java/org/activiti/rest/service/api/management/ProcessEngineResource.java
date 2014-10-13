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

package org.activiti.rest.service.api.management;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.engine.ProcessEngines;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tijs Rademakers
 */
@RestController
public class ProcessEngineResource {
  
  @Autowired @Qualifier("processEngine")
  protected ProcessEngine engine;

  @RequestMapping(value="/management/engine", method = RequestMethod.GET, produces = "application/json")
  public ProcessEngineInfoResponse getEngineInfo() {
    ProcessEngineInfoResponse response = new ProcessEngineInfoResponse();
    
    try {
      ProcessEngineInfo engineInfo = ProcessEngines.getProcessEngineInfo(engine.getName());
      if (engineInfo != null) {
        response.setName(engineInfo.getName());
        response.setResourceUrl(engineInfo.getResourceUrl());
        response.setException(engineInfo.getException());
      } else {
        // Revert to using process-engine directly
        response.setName(engine.getName());
      }
    } catch (Exception e) {
      throw new ActivitiException("Error retrieving process info", e);
    }
   
    response.setVersion(ProcessEngine.VERSION);
    return response;
  }
}