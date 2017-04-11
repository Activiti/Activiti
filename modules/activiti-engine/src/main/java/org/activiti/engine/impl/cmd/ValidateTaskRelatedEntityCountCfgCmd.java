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
package org.activiti.engine.impl.cmd;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.PropertyEntity;
import org.activiti.engine.impl.persistence.entity.PropertyEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateTaskRelatedEntityCountCfgCmd implements Command<Void> {

  private static final Logger logger = LoggerFactory.getLogger(ValidateTaskRelatedEntityCountCfgCmd.class);
  
  public static String PROPERTY_TASK_RELATED_ENTITY_COUNT = "cfg.task-related-entities-count";
	  
  @Override
  public Void execute(CommandContext commandContext) {

    PropertyEntityManager propertyEntityManager = commandContext.getPropertyEntityManager();
	    
    boolean configProperty = commandContext.getProcessEngineConfiguration().getPerformanceSettings().isEnableTaskRelationshipCounts();
    PropertyEntity propertyEntity = propertyEntityManager.findById(PROPERTY_TASK_RELATED_ENTITY_COUNT);
	    
    if(propertyEntity == null) {      
      //In case the property is not present in table queried above, simply insert the value	      
	  PropertyEntity newPropertyEntity = propertyEntityManager.create();
	  newPropertyEntity.setName(PROPERTY_TASK_RELATED_ENTITY_COUNT);
	  newPropertyEntity.setValue(Boolean.toString(configProperty));
	  propertyEntityManager.insert(newPropertyEntity);
	      
    }else {
	  boolean propertyValue = Boolean.valueOf(propertyEntity.getValue().toLowerCase());
	  //It will impact the performance in the positive sense when a lot tasks will be present, say thousands.
	  if (!configProperty && propertyValue) {
	    if (logger.isInfoEnabled()) {
	      logger.info("Configuration change: task related entity counting feature was enabled before, but now disabled. "
	        + "Updating all task entities.");
	    }
	    commandContext.getProcessEngineConfiguration().getTaskDataManager().updateAllTaskRelatedEntityCountFlags(configProperty);
	  }    
	  // Update property
	  if (configProperty != propertyValue) {
	    propertyEntity.setValue(Boolean.toString(configProperty));
	    propertyEntityManager.update(propertyEntity);
	  }
	      
  }
	    
    return null;
  }

}