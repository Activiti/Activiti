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
package org.activiti.engine.impl.util;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class Activiti5Util {
  
  public static boolean isActiviti5ProcessDefinitionId(CommandContext commandContext, final String processDefinitionId) {
    
    if (processDefinitionId == null) {
      return false;
    }
    
    try {
      ProcessDefinition processDefinition = ProcessDefinitionUtil.getProcessDefinition(processDefinitionId);
      if (processDefinition == null) {
        return false;
      }
      return isActiviti5ProcessDefinition(commandContext, processDefinition);
    } catch (ActivitiObjectNotFoundException e) {
      return false;
    }
  }
  
  /**
   * Use this method when running outside a {@link CommandContext}. 
   * It will check the cache first and only start a new {@link CommandContext} when no result is found in the cache. 
   */
  public static boolean isActiviti5ProcessDefinitionId(final ProcessEngineConfigurationImpl processEngineConfiguration, final String processDefinitionId) {
    
    if (processDefinitionId == null) {
      return false;
    }
    
    if (!processEngineConfiguration.isActiviti5CompatibilityEnabled()) {
      return false;
    }
    
    ProcessDefinitionCacheEntry cacheEntry = processEngineConfiguration.getProcessDefinitionCache().get(processDefinitionId);
    if (cacheEntry != null) {
      ProcessDefinition processDefinition = cacheEntry.getProcessDefinition();
      return Activiti5CompatibilityHandler.ACTIVITI_5_ENGINE_TAG.equals(processDefinition.getEngineVersion());
    } else {
      return processEngineConfiguration.getCommandExecutor().execute(new Command<Boolean>() {
        
        @Override
        public Boolean execute(CommandContext commandContext) {
          return isActiviti5ProcessDefinitionId(commandContext, processDefinitionId);
        }
        
      });
      
    }
  }
  
  
  public static boolean isActiviti5ProcessDefinition(CommandContext commandContext, ProcessDefinition processDefinition) {
    
    if (!commandContext.getProcessEngineConfiguration().isActiviti5CompatibilityEnabled()) {
      return false;
    }
    
    if (processDefinition.getEngineVersion() != null) {
      if (Activiti5CompatibilityHandler.ACTIVITI_5_ENGINE_TAG.equals(processDefinition.getEngineVersion())) {
        if (commandContext.getProcessEngineConfiguration().isActiviti5CompatibilityEnabled()) {
          return true;
        }
      } else {
        throw new ActivitiException("Invalid 'engine' for process definition " + processDefinition.getId() + " : " + processDefinition.getEngineVersion());
      }
    }
    return false;
  }
  
  public static Activiti5CompatibilityHandler getActiviti5CompatibilityHandler() {
    Activiti5CompatibilityHandler activiti5CompatibilityHandler = Context.getActiviti5CompatibilityHandler();
    if (activiti5CompatibilityHandler == null) {
      activiti5CompatibilityHandler = Context.getFallbackActiviti5CompatibilityHandler();
    }
    
    if (activiti5CompatibilityHandler == null) {
      throw new ActivitiException("Found Activiti 5 process definition, but no compatibility handler on the classpath");
    }
    return activiti5CompatibilityHandler;
  }

}
