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
