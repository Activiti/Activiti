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

package org.activiti.compatibility;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti5.engine.ActivitiException;
import org.activiti5.engine.ProcessEngine;
import org.activiti5.engine.parse.BpmnParseHandler;


public class DefaultProcessEngineFactory {

  /**
   * Takes in an Activiti 6 process engine config, gives back an Activiti 5 Process engine.
   */
  public ProcessEngine buildProcessEngine(ProcessEngineConfigurationImpl activiti6Configuration) {

    // TODO: jta/spring/custom type

    org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration = null;
    if (activiti6Configuration instanceof StandaloneProcessEngineConfiguration) {
      activiti5Configuration = new org.activiti5.engine.impl.cfg.StandaloneProcessEngineConfiguration();

      activiti5Configuration.setDataSource(activiti6Configuration.getDataSource());

    } else {
      throw new ActivitiException("Unsupported process engine configuration");
    }
    
    convertParseHandlers(activiti6Configuration, activiti5Configuration);

    return activiti5Configuration.buildProcessEngine();

  }

  protected void convertParseHandlers(ProcessEngineConfigurationImpl activiti6Configuration, org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5Configuration) {
    activiti5Configuration.setPreBpmnParseHandlers(convert(activiti6Configuration.getActiviti5PreBpmnParseHandlers()));
    activiti5Configuration.setPostBpmnParseHandlers(convert(activiti6Configuration.getActiviti5PostBpmnParseHandlers()));
    activiti5Configuration.setCustomDefaultBpmnParseHandlers(convert(activiti6Configuration.getActiviti5CustomDefaultBpmnParseHandlers()));
  }
  
  protected List<BpmnParseHandler> convert(List<Object> activiti5BpmnParseHandlers) {
    if (activiti5BpmnParseHandlers == null) {
      return null;
    }
      
    List<BpmnParseHandler> parseHandlers = new ArrayList<BpmnParseHandler>(activiti5BpmnParseHandlers.size());
    for (Object activiti6BpmnParseHandler : activiti5BpmnParseHandlers) {
      parseHandlers.add((BpmnParseHandler) activiti6BpmnParseHandler);
    }
    return parseHandlers;
  }
  
}
