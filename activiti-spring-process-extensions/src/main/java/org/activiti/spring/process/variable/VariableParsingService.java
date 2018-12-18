package org.activiti.spring.process.variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.types.ExtensionVariableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariableParsingService {

    private static final Logger logger = LoggerFactory.getLogger(VariableParsingService.class);

    public VariableParsingService(Map<String, ExtensionVariableType> variableTypeMap) {
        this.variableTypeMap = variableTypeMap;
    }

    private Map<String, ExtensionVariableType> variableTypeMap;

    public Object parse(VariableDefinition variableDefinition) throws ActivitiException{


        if(variableDefinition.getType()!=null) {
            ExtensionVariableType type = variableTypeMap.get(variableDefinition.getType());

            return type.parseFromString(String.valueOf(variableDefinition.getValue()));
        }

        return variableDefinition.getValue();
    }

}
