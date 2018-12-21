package org.activiti.spring.process.variable.types;

import java.util.List;

import org.activiti.engine.ActivitiException;

/**
 * Base variable type for types as defined in extension json files.
 * Used to validate variables against definition.
 */
public abstract class VariableType {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    abstract public void validate(Object var, List<ActivitiException> errors);

    public Object parseFromValue(Object value) throws ActivitiException {
        return value;
    }
}
