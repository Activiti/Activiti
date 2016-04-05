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
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

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
      ProcessDefinitionEntity processDefinitionEntity = ProcessDefinitionUtil.getProcessDefinitionEntity(processDefinitionId);
      if (processDefinitionEntity == null) {
        return false;
      }
      return isActiviti5ProcessDefinition(commandContext, processDefinitionEntity);
    } catch (ActivitiObjectNotFoundException e) {
      return false;
    }
  }
  
  public static boolean isActiviti5ProcessDefinition(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity) {
    
    if (!commandContext.getProcessEngineConfiguration().isActiviti5CompatibilityEnabled()) {
      return false;
    }
    
    if (processDefinitionEntity.getEngineVersion() != null) {
      if (Activiti5CompatibilityHandler.ACTIVITI_5_ENGINE_TAG.equals(processDefinitionEntity.getEngineVersion())) {
        if (commandContext.getProcessEngineConfiguration().isActiviti5CompatibilityEnabled()) {
          return true;
        }
      } else {
        throw new ActivitiException("Invalid 'engine' for process definition " + processDefinitionEntity.getId() + " : " + processDefinitionEntity.getEngineVersion());
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
