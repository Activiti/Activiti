package org.activiti.spring.process.variable.types;

import java.util.List;

import org.activiti.common.util.DateFormatterProvider;
import org.activiti.engine.ActivitiException;

/**
 * Basic date type for setting default date values for vars in extension json
 */
public class DateVariableType extends JavaObjectVariableType {

    private final DateFormatterProvider dateFormatterProvider;
    
    public DateVariableType(Class clazz, DateFormatterProvider dateFormatterProvider) {
        super(clazz);
        this.dateFormatterProvider = dateFormatterProvider;
    }

    @Override
    public void validate(Object var, List<ActivitiException> errors) {
        super.validate(var, errors);
    }

    @Override
    public Object parseFromValue(Object value) throws ActivitiException {

        try {
            return dateFormatterProvider.toDate(value);
        } catch (Exception e) {
            throw new ActivitiException("Error parsing date value " + value, e);
        }
    }
}
