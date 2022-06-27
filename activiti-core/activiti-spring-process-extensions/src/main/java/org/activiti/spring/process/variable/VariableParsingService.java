/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.spring.process.variable;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.types.VariableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to read variable values as per their expected types from the proc extension json
 */
public class VariableParsingService {

    private static final Logger logger = LoggerFactory.getLogger(VariableParsingService.class);

    public VariableParsingService(Map<String, VariableType> variableTypeMap) {
        this.variableTypeMap = variableTypeMap;
    }

    private Map<String, VariableType> variableTypeMap;

    public Object parse(VariableDefinition variableDefinition) throws ActivitiException{


        if(variableDefinition.getType()!=null) {
            VariableType type = variableTypeMap.get(variableDefinition.getType());

            return type.parseFromValue(variableDefinition.getValue());
        }

        return variableDefinition.getValue();
    }

}
