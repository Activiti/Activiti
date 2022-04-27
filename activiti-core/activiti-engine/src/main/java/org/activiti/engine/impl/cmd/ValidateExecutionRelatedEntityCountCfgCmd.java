/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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


public class ValidateExecutionRelatedEntityCountCfgCmd implements Command<Void> {

  private static final Logger logger = LoggerFactory.getLogger(ValidateExecutionRelatedEntityCountCfgCmd.class);

  public static String PROPERTY_EXECUTION_RELATED_ENTITY_COUNT = "cfg.execution-related-entities-count";

  @Override
  public Void execute(CommandContext commandContext) {

    /*
     * If execution related entity counting is on in config | Current property in database : Result
     *
     *  A) true | not there : write new property with value 'true'
     *  B) true | true : all good
     *  C) true | false : the feature was disabled before, but it is enabled now. Old executions will have a local flag with false.
     *                    It is now enabled. This is fine, will be handled in logic. Update the property.
     *
     *  D) false | not there: write new property with value 'false'
     *  E) false | true : the feature was disabled before and enabled now. To guarantee data consistency, we need to remove the flag from all executions.
     *                    Update the property.
     *  F) false | false : all good
     *
     * In case A and D (not there), the property needs to be written to the db
     * Only in case E something needs to be done explicitely, the others are okay.
     */

    PropertyEntityManager propertyEntityManager = commandContext.getPropertyEntityManager();

    boolean configProperty = commandContext.getProcessEngineConfiguration().getPerformanceSettings().isEnableExecutionRelationshipCounts();
    PropertyEntity propertyEntity = propertyEntityManager.findById(PROPERTY_EXECUTION_RELATED_ENTITY_COUNT);

    if (propertyEntity == null) {

      // 'not there' case in the table above: easy, simply insert the value

      PropertyEntity newPropertyEntity = propertyEntityManager.create();
      newPropertyEntity.setName(PROPERTY_EXECUTION_RELATED_ENTITY_COUNT);
      newPropertyEntity.setValue(Boolean.toString(configProperty));
      propertyEntityManager.insert(newPropertyEntity);

    } else {

      boolean propertyValue = Boolean.valueOf(propertyEntity.getValue().toString().toLowerCase());
      if (!configProperty && propertyValue) {
        if (logger.isInfoEnabled()) {
          logger.info("Configuration change: execution related entity counting feature was enabled before, but now disabled. "
              + "Updating all execution entities.");
        }
        commandContext.getProcessEngineConfiguration().getExecutionDataManager().updateAllExecutionRelatedEntityCountFlags(configProperty);
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
