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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.types.VariableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariableValidationService {

    private static final Logger logger = LoggerFactory.getLogger(VariableValidationService.class);

    public VariableValidationService(Map<String, VariableType> variableTypeMap) {
        this.variableTypeMap = variableTypeMap;
    }

    private Map<String, VariableType> variableTypeMap;

    public boolean validate(Object var, VariableDefinition variableDefinition){
        return validateWithErrors(var,variableDefinition).isEmpty();
    }

    public List<ActivitiException> validateWithErrors(Object var, VariableDefinition variableDefinition){

        List<ActivitiException> errors = new ArrayList<>();

        if(variableDefinition.getType()!=null) {
            VariableType type = variableTypeMap.get(variableDefinition.getType());

            //if type is not in the map then assume to be json
            if(type==null){
                type = variableTypeMap.get("json");
            }

            type.validate(var,
                          errors);
        } else{
            errors.add(new ActivitiException(variableDefinition.getName()+" has no type"));
            logger.error(variableDefinition.getName()+" has no type");
        }

        return errors;
    }

}
