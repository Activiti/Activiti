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
