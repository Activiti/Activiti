package org.activiti.spring.process.variable.types;

import java.time.DateTimeException;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.spring.process.variable.DateFormatterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic date type for setting default date values for vars in extension json
 */
public class DateVariableType extends JavaObjectVariableType {

    public static String defaultFormat = "yyyy-MM-dd";
    private static final Logger logger = LoggerFactory.getLogger(DateVariableType.class);
    private final DateFormatterProvider dateFormatterProvider;
    
    public DateVariableType(Class clazz, DateFormatterProvider dateFormatterProvider) {
        super(clazz);
        this.dateFormatterProvider = dateFormatterProvider;
    }

    @Override
    public void validate(Object var, List<ActivitiException> errors) {
        super.validate(var,errors);
    };

    @Override
    public Object parseFromValue(Object value) throws ActivitiException {

        try {
            return dateFormatterProvider.convert2Date(value);
        } catch (DateTimeException e) {
            throw new ActivitiException("Error parsing date value " + value, e);
        }
    }
}
